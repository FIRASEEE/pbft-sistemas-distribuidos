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
	private ArrayList<Integer> comisiones;
	private boolean compromisoEnviado;
	private boolean comisionEnviada;
	private final Object lock= new Object();
	public Proceso(int id,int totalProcesos) {
		this.id = id;
		this.error = false;
		this.totalProcesos= totalProcesos;
		this.quorum=(this.totalProcesos/2)+1;
		this.compromisos= new ArrayList<>();
		this.comisiones= new ArrayList<>();
		this.compromisoEnviado= false;
		this.comisionEnviada= false;
		
		
	}

	@Override
	public void run() {
	}
	
	public void reiniciar() {
		synchronized(this) {
			this.variable=-1;
			this.compromisos.clear();
			this.comisiones.clear();
			this.compromisoEnviado= false;
			this.comisionEnviada= false;
		}
	}
	
	public int propuesta(int valor) {
		if (error){
			return new Random().nextInt(101);
		}
		 else {
			return valor;
		}
		
	}
	
	public int compromiso(int valor) {
		synchronized(this) {
			this.compromisos.add(valor);
			System.out.println("lista compromisos size "+compromisos.size()+"de proceso "+this.id);
			if (compromisoEnviado) return -1;
		
	
			int frecuencia=Collections.frequency(compromisos, valor);
			if (frecuencia>=this.quorum) {
				this.compromisoEnviado= true;
				this.variable=valor;
				return this.variable;
			}
		}
		return -1;
	}



	public int comision(int valor) {
		synchronized(this) {
			this.comisiones.add(valor);
			if (comisionEnviada) return -1;
			int frecuencia=Collections.frequency(comisiones, valor);
			if (frecuencia>=this.quorum) {
				this.comisionEnviada= true;
				synchronized(lock) {
					lock.notifyAll();
				}
				return valor;
			}
		}
		return -1;
	}
	public void esperarConfirmacion(int timeout) {
		long inicio = System.currentTimeMillis();
		while (this.variable==-1 && System.currentTimeMillis() - inicio < timeout) {
			try {
				synchronized(lock) {
					lock.wait(timeout);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			
			}
		}
	}
		
	
	
	public String confirmacion() {
		return "Valor final confirmado: "+this.variable;
	}
	
	
	
	public int getProcessId() {
		return this.id;
	}
	public void setProcessId(int id) {
		this.id = id;
	}
    public void modificarError() {
    	this.error=!this.error;
    }
    public boolean isError() {
		return this.error;
	}

	public String getCompromisosString() {
		return compromisos.toString();
	}
	public String variableToString() {
		if (this.variable==-1) {
			return "-";
		}
		return String.valueOf(this.variable);
	}

}
