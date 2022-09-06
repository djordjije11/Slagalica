package main;

import java.io.BufferedReader;
import java.io.IOException;

public class ExitThread extends Thread {

	private BufferedReader clientInput = null;
	private WaitMonitor waiter;
	private ClientHandler client;
	
	public ExitThread(WaitMonitor waiter, ClientHandler client, BufferedReader clientInput) {
		this.waiter = waiter;
		this.client = client;
		this.clientInput = clientInput;
	}
	
	@Override
	public void run() {
		try {
			/*
			String line;
			while((line = clientInput.readLine()) == null || (line != null && !line.equals("EXIT"))) {
				if(interrupted()) {
					return;
				}
			}
			client.setIsQuit(true);
			synchronized(waiter) {
				waiter.notify();	//obavestava se ClientHandler instanca da je njena odgovarajuca Client instanca napustila igru
			}
			*/
			while(!clientInput.ready()) {
				if(interrupted()) {
					return;
				}
			}
			if(clientInput.readLine().equals("EXIT")) {
				client.setIsQuit(true);
				synchronized(waiter) {
					waiter.notify();	//obavestava se ClientHandler instanca da je njena odgovarajuca Client instanca napustila igru
				}
			}
		} catch(IOException e) {
			client.setIsQuit(true);
			synchronized(waiter) {
				waiter.notify();	//obavestava se ClientHandler instanca da je njena odgovarajuca Client instanca napustila igru
			}
		}
	}
}
