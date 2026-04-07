package com.pbft.servidor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
public class Proceso extends Thread {
	private int id;
	private boolean error;
	private int variable;
	private int totalProcesos;
	private int quorum;
	private ArrayList<Integer> compromisos;
	private  ArrayList<Integer> comisiones;

	public Proceso(int id,int totalProcesos) {
		this.id = id;
		this.error = false;
		this.totalProcesos= totalProcesos;
		this.quorum=(this.totalProcesos/2)+1;
		this.compromisos= new ArrayList<>();
		this.comisiones= new ArrayList<>();
	}

	@Override
	public void run() {
	}
	
	public int propuesta(int valor) {
		this.variable= -1;
		compromisos.clear();
		comisiones.clear();
		if (error){
			return new Random().nextInt(101);
		}
		 else {
			return valor;
		}
		
	}
	
	public int compromiso(int valor) {
		this.compromisos.add(valor);
		int frecuencia=Collections.frequency(compromisos, valor);
		if (frecuencia>=this.quorum) {
			this.variable=valor;
			return this.variable;
		}
		
		return -1;
	}
	
	public int comision(int valor) {
		this.comisiones.add(valor);
		int frecuencia=Collections.frequency(comisiones, valor);
		if (frecuencia>=this.quorum) {
			return this.confirmacion();
		}
		
		return -1;
	}
	
	public int confirmacion() {
		return this.variable;
	}
	
	
	
	public long getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean getError() {
		return this.error;
	}
	public void setError(boolean error) {
		this.error = error;
	}

}
