package com.pbft.cliente;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

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
		for (Map.Entry<Integer, String> entry : procesosRemotos.entrySet()) {
		    int idProceso = entry.getKey();
		    String direccionProceso = entry.getValue();
		    URI uri = UriBuilder.fromUri("http://" + direccionProceso).build();
		    WebTarget target = client.target(uri);
		    String respuesta=target.path("rest").path("servicio").path("propuesta")
		        .queryParam("valor", valor)
		        .request()
		        .get(String.class);
		    
		    System.out.println("Respuesta del proceso " + idProceso + ": " + respuesta);
		    
		    
		}
	}
	public void fallo(int idProceso) {
		String direccionProceso=procesosRemotos.get(idProceso);
		URI uri = UriBuilder.fromUri("http://" + direccionProceso).build();
		WebTarget target = client.target(uri);
		String respuesta = target.path("rest").path("servicio").path("fallo").request().get(String.class);
		System.out.println("Respuesta del proceso " + idProceso + ": " + respuesta);
	}
	
	public void estado() {
	
	}
		
	
	public static void main(String[] args) {
	     int opcion=0;
	     System.out.println("Bienvenido al cliente PBFT");
	     System.out.println("Seleccione una opción:");
	     System.out.println("1. Fallo sN (n es el número del proceso que falla)");
	     System.out.println("2. cambiar el valor sX (X es el nuevo valor a enviar)");
	     System.out.println("3.estado (s) para mostrar el estado de los procesos");
	     System.out.println("4 ayuda(h) para mostrar las opciones");
	     System.out.println("5. Salir");
	     
	     switch (opcion) {
	        case 1:
	            // Lógica para simular el fallo de un proceso
	            break;
	        case 2:
	            // Lógica para cambiar el valor a enviar
	            break;
	        case 3:
	            // Lógica para mostrar el estado de los procesos
	            break;
	        case 4:
	            // Lógica para mostrar las opciones de ayuda
	            break;
	        case 5:
	            System.out.println("Saliendo del cliente PBFT...");
	            break;
	        default:
	            System.out.println("Opción no válida. Por favor, seleccione una opción válida.");
	    
	}	

	}
}
