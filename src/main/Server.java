package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
	public static LinkedList<ClientHandler> onlineUsers = new LinkedList<>();
	public static void main(String[] args) {
		ServerSocket socket;
		Socket socketCommunication;
		int port = 9001;
		try {
			socket = new ServerSocket(port);
			System.out.println("Server je uspesno pokrenut.");
			WaitMonitor waiterPair = new WaitMonitor();
			WaitMonitor waiterMojBroj = new WaitMonitor();
			while(true) {
				//System.out.println("Cekam na konekciju...");
				socketCommunication = socket.accept();
				System.out.println("Konekcija je uspostavljena!");
				ClientHandler client = new ClientHandler(socketCommunication, waiterPair, waiterMojBroj);
				client.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
