package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
	public static LinkedList<ClientHandler> onlineUsers = new LinkedList<>();
	public static LinkedList<WaitMonitor> waitersPair = new LinkedList<>();
	public static LinkedList<WaitMonitor> waitersMojBroj = new LinkedList<>();
	public static void main(String[] args) {
		ServerSocket socket;
		Socket socketCommunication;
		int port = 9001;
		int connectionCounter = 0;
		try {
			socket = new ServerSocket(port);
			System.out.println("Server je uspesno pokrenut.");
			WaitMonitor waiterPair = null;
			while(true) {
				if(connectionCounter % 2 == 0) {
					waiterPair = new WaitMonitor();
					waitersPair.add(waiterPair);
				}
				socketCommunication = socket.accept();
				connectionCounter++;
				System.out.println("Konekcija je uspostavljena!");
				ClientHandler client = new ClientHandler(socketCommunication);
				client.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
