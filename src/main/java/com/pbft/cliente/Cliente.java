package com.pbft.cliente;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.UriBuilder;

public class Cliente {
	
	
	private Map <Integer, String> procesosRemotos;
	Properties config;
	Client client;
	public Cliente() throws Exception {
		client=ClientBuilder.newClient();
		config =new Properties();
		String ruta=System.getProperty("config.path");
		config.load(new java.io.FileInputStream(ruta));
		procesosRemotos= new java.util.HashMap<>();
		int totalProcesos=Integer.parseInt(config.getProperty("total.procesos"));
		for (int i=1;i<=totalProcesos;i++) {
			String ipProceso=config.getProperty("proceso."+i+".ip");
			int puertoProceso=Integer.parseInt(config.getProperty("proceso."+i+".puerto"));
			String dirProceso=ipProceso+":"+puertoProceso;
			procesosRemotos.put(i, dirProceso);
		}
		
	}
	public void propuesta(int valor) {
		
		Set <String> serviciosConsultados= new HashSet<>(procesosRemotos.values());
		for (String servicio : serviciosConsultados) {
			URI uri = UriBuilder.fromUri("http://" + servicio+"/pbft").build();
			WebTarget target = client.target(uri);
			String respuesta = target.path("rest").path("servicio").path("reiniciar")
			    .request()
			    .get(String.class);
			System.out.println("Respuesta de " + servicio + ": " + respuesta);
		}
		
	    for (Map.Entry<Integer, String> entry : procesosRemotos.entrySet()) {
	        int idProceso = entry.getKey();
	        String direccionProceso = entry.getValue();
	        System.out.println("Enviando propuesta a proceso " + idProceso + " en " + direccionProceso);
	        new Thread(() -> {
	            URI uri = UriBuilder.fromUri("http://" + direccionProceso+"/pbft").build();
	            WebTarget target = client.target(uri);
	            String respuesta = target.path("rest").path("servicio").path("propuesta")
	                .queryParam("valor", valor)
	                .queryParam("procesoId", idProceso)
	                .request()
	                .get(String.class);
	            System.out.println("P" + idProceso + " → " + respuesta);
	        }).start();
	    }
	}
	public void fallo(int idProceso) {
		String direccionProceso=procesosRemotos.get(idProceso);
		URI uri = UriBuilder.fromUri("http://" + direccionProceso +"/pbft").build();
		WebTarget target = client.target(uri);
		String respuesta = target.path("rest").path("servicio").path("fallo").queryParam("procesoId", idProceso).request().get(String.class);
		System.out.println("Respuesta del proceso " + idProceso + ": " + respuesta);
	}
	
	public void estado() {
		
		Set <String> serviciosConsultados= new HashSet<>(procesosRemotos.values());
		System.out.println("id\tvar\tcompromisos\terror");
		for (String servicio : serviciosConsultados) {
			URI uri = UriBuilder.fromUri("http://" + servicio+"/pbft").build();
			WebTarget target = client.target(uri);
			String respuesta = target.path("rest").path("servicio").path("estado").request().get(String.class);
			System.out.print(respuesta);
		}
		
	
	}
		
	
	public static void main(String[] args) throws Exception {
	    Cliente cliente = new Cliente();
	    java.util.Scanner scanner = new java.util.Scanner(System.in);
	    
	    System.out.println("Cliente PBFT iniciado. Escribe 'h' para ayuda.");
	    
	    while (true) {
	        System.out.print("> ");
	        String input = scanner.nextLine().trim();
	        
	        if (input.equals("h")) {
	            System.out.println("fN  - Activar/desactivar fallo en proceso N");
	            System.out.println("sX  - Proponer cambiar valor a X");
	            System.out.println("s   - Mostrar estado");
	            System.out.println("h   - Ayuda");
	        } else if (input.equals("s")) {
	            cliente.estado();
	        } else if (input.startsWith("f")) {
	            int id = Integer.parseInt(input.substring(1));
	            cliente.fallo(id);
	        } else if (input.startsWith("s")) {
	            int valor = Integer.parseInt(input.substring(1));
	            cliente.propuesta(valor);
	        }
	    }
	}
}
