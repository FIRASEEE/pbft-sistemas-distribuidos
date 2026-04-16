package com.pbft.servidor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

@Singleton
@Path("servicio")
public class Servicio {
	
	private Map <Integer, Proceso> procesosLocales;
	private Map <Integer, String> procesosRemotos;
	private String miDireccion;
	private String miPuerto;
	private int totalProcesos;
	Client client;
	Properties config;
	public Servicio() throws Exception, IOException {
		miPuerto=System.getProperty("puerto");
		procesosLocales= new HashMap<>();
		procesosRemotos= new HashMap<>();

		config =new Properties();
		String ruta=System.getProperty("config.path");
		config.load(new FileInputStream(ruta));
		totalProcesos=Integer.parseInt(config.getProperty("total.procesos"));
		miDireccion=resolverDireccion();
		client=ClientBuilder.newClient();
		iniciar();
		}
	
	private String resolverDireccion() {
		for (int i=1;i<=totalProcesos;i++) {
			String puerto=config.getProperty("proceso."+i+".puerto");
			if (miPuerto.equals(puerto)) {
				String ip=config.getProperty("proceso."+i+".ip");
				return ip+":"+miPuerto;
			}
		}
		return "";
	}

	public void iniciar() {
			
		int puerto=Integer.parseInt(miPuerto);
		for (int i=1;i<=totalProcesos;i++) {
			String ipProceso=config.getProperty("proceso."+i+".ip");
			int puertoProceso=Integer.parseInt(config.getProperty("proceso."+i+".puerto"));
			String dirProceso=ipProceso+":"+puertoProceso;
			if(dirProceso.equals(miDireccion)) {
				Proceso procesoLocal=new Proceso(i,totalProcesos);
				procesosLocales.put(i, procesoLocal);
				procesoLocal.start();
			}
			else {
				procesosRemotos.put(i, dirProceso);
			}
		}
	}
	@GET
	@Path("reiniciar")
	public String reiniciar() {
		for (Proceso proceso: procesosLocales.values()) {
			proceso.reiniciar();
		}
		return "Procesos reiniciados";
	}
		
	@GET
	@Path("propuesta")
	public String propuesta(@QueryParam("valor") int valor,@QueryParam("procesoId") int procesoId) {
		System.out.println("Recibida propuesta de P"+procesoId+" con valor "+valor);
		int valorPropuesta=-1;
		int valorComision=-1;
		Proceso procesoLocal=procesosLocales.get(procesoId);
		
		if (procesoLocal!=null) {
			valorPropuesta=procesoLocal.propuesta(valor);
			
		}

		 enviarCompromisos(valorPropuesta, procesoId);
		 
		 
		 procesoLocal.esperarConfirmacion(30000);

		 return procesoLocal.variableToString();
	
	}
	
	@GET
	@Path("compromiso")
	public int compromiso(@QueryParam("valor") int valor,@QueryParam("procesoId") int procesoId,@QueryParam("emisorId") int emisorId) {
		int valorCompromiso=-1;
		Proceso procesoLocal=procesosLocales.get(procesoId);
		if (procesoLocal!=null) {
			valorCompromiso=procesoLocal.compromiso(valor);
		}
		if (valorCompromiso!=-1) {
			enviarComisiones(valorCompromiso, emisorId);
		}
		return valorCompromiso;
	}
	
	@GET
	@Path("comision")
	public int comision(@QueryParam("valor") int valor,@QueryParam("procesoId") int procesoId,@QueryParam("emisorId") int emisorId) {
		int valorComision=-1;
		Proceso procesoLocal=procesosLocales.get(procesoId);
		if (procesoLocal!=null) {
			valorComision=procesoLocal.comision(valor);
		}
		if (valorComision!=-1) {
			enviarConfirmacion(valorComision, procesoId);
		}
		return valorComision;
	}
	
	@GET
	@Path("fallo")
	public String fallo(@QueryParam("procesoId") int procesoId) {
		Proceso procesoLocal=procesosLocales.get(procesoId);
		if (procesoLocal!=null) {
			procesoLocal.modificarError();;
			return "Fallo registrado en proceso "+procesoId;
		}
		return "Proceso "+procesoId+" no encontrado";
	}
	
	@GET
	@Path("estado")
	public String estado() {
		StringBuilder estado= new StringBuilder();
		 estado.append("id\tvar\tcompromisos\terror\n");
		for (Proceso p: procesosLocales.values()) {
			estado.append(p.getProcessId() + "\t");
	        estado.append(p.variableToString() + "\t");
	        estado.append(p.getCompromisosString() + "\t");
	        estado.append(p.isError() + "\n");
		}
		return estado.toString();
	}
	
	

	public void enviarCompromisos(int valorPropuesta, int procesoId) {
		for (int i=1;i<=totalProcesos;i++) {
			if(procesosLocales.containsKey(i)) {
				Proceso procesoLocal=procesosLocales.get(i);
				int valorCompromiso=procesoLocal.compromiso(valorPropuesta);
				System.out.println("se ha ejecutado compromiso proceso local "+valorCompromiso);
				if (valorCompromiso!=-1) {
					enviarComisiones(valorCompromiso, i);
				}
			}
			else {
				String dirProceso=procesosRemotos.get(i);
				URI uri=UriBuilder.fromUri("http://"+dirProceso).build();
				WebTarget target=client.target(uri)
						.path("rest")
						.path("servicio")
						.path("compromiso")
						.queryParam("valor", valorPropuesta)
						.queryParam("procesoId", i)
						.queryParam("emisorId", procesoId);
				int valorCompromiso=target.request().get(Integer.class);
			}
			
		}
	}

	private void enviarComisiones(int valorCompromiso, int procesoId) {
		for (int i=1;i<=totalProcesos;i++) {
			if(procesosLocales.containsKey(i)) {
				Proceso procesoLocal=procesosLocales.get(i);
				int valorComision=procesoLocal.comision(valorCompromiso);
				if (valorComision!=-1) {
					enviarConfirmacion(valorComision, i);
				}
			}
			else {
				String dirProceso=procesosRemotos.get(i);
				URI uri=UriBuilder.fromUri("http://"+dirProceso).build();
				WebTarget target=client.target(uri)
						.path("rest")
						.path("servicio")
						.path("comision")
						.queryParam("valor", valorCompromiso)
						.queryParam("procesoId", i)
						.queryParam("emisorId", procesoId);
				int valorConfirmacion=target.request().get(Integer.class);
			}
			
		}
		
	}

	private String enviarConfirmacion(int valorComision, int procesoId) {
		Proceso procesoLocal=procesosLocales.get(procesoId);
		if (procesoLocal!=null) {
			return procesoLocal.confirmacion();
		}
		return "error";
		
	}
}