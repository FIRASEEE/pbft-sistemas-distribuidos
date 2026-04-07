package com.pbft.cliente;

public class Cliente {
	
	
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
