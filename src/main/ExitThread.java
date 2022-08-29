package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ExitThread extends Thread {

	private BufferedReader clientInput = null;
	private Socket socketCommunication = null;
	private WaitMonitor waiter;
	private ClientHandler client;
	
	public ExitThread(WaitMonitor waiter, ClientHandler client, Socket socketCommunication) {
		this.waiter = waiter;
		this.client = client;
		this.socketCommunication = socketCommunication;
	}
	
	@Override
	public void run() {
		try {
			clientInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
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
			e.printStackTrace();
		}
	}
}
