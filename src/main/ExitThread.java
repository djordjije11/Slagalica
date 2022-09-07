package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class ExitThread extends Thread {

	private BufferedReader clientInput = null;
	private WaitMonitor waiter;
	private ClientHandler client;
	private boolean isExit = false;
	
	public boolean getIsExit() {
		return isExit;
	}
	public ExitThread(WaitMonitor waiter, ClientHandler client, BufferedReader clientInput) {
		this.waiter = waiter;
		this.client = client;
		this.clientInput = clientInput;
	}
	
	@Override
	public void run() {
		//koriscenje Random klase moze da posluzi za proveravanje toga da li se svaka pokrenuta nit zavrsila
		int a = new Random().nextInt(1000);
		try {
			System.out.println(a + ". EXIT Nit se pokrenula");
			clientInput.readLine();
			
			
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
			/*
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
			*/
			System.out.println(a + ". EXIT Nit se zavrsila");
		} catch(IOException e) {
			isExit = true;
			client.setIsQuit(true);
			synchronized(waiter) {
				waiter.notify();	//obavestava se ClientHandler instanca da je njena odgovarajuca Client instanca napustila igru
			}
			System.out.println(a + ". EXIT Nit se zavrsila");
		}
	}
}
