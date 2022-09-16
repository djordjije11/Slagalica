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
		//using the Random class can be used to check whether each running thread has finished
		int a = new Random().nextInt(1000);
		try {
			System.out.println(a + ". EXIT Nit se pokrenula");
			clientInput.readLine();
			System.out.println(a + ". EXIT Nit se zavrsila");
		} catch(IOException e) {
			isExit = true;
			client.setIsQuit(true);
			synchronized(waiter) {
				waiter.notify();	//notifying the ClientHandler instance that the client has quit
			}
			System.out.println(a + ". EXIT Nit se zavrsila");
		}
	}
}