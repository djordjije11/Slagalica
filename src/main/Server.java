package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

import quizClasses.Questions;

public class Server {
	public static LinkedList<ClientHandler> onlineUsers = new LinkedList<ClientHandler>();
	public static LinkedList<WaitMonitor> waitersPair = new LinkedList<WaitMonitor>();
	public static LinkedList<Questions> questionsList = new LinkedList<Questions>();
	public static LinkedList<String> codesList = new LinkedList<String>();
	
	private static void generateQuestionsList() {
		Questions pitanje1 = new Questions(new String[]{"Scarface", "Dog Day Afternoon", "Mystic River", "Donnie Brasco"}, 
				"U kom od navedenih filmova ne glumi Al Pacino?", "Mystic River");
		Questions pitanje2 = new Questions(new String[] {"1789", "1889", "1389", "1689"},
				"Koje godine je pocela francuska revolucija?", "1789");
		Questions pitanje3 = new Questions(new String[]{"Borislav Pekic", "Lav Tolstoj", "Irving Stoun", "Herman Hese"}, 
				"Koji pisac je napisao knjigu Stepski vuk?", "Herman Hese");
		Questions pitanje4 = new Questions(new String[]{"5", "2", "4", "6"}, 
				"Za koliko razlicitih fudbalskih klubova je igrao Kristijano Ronaldo", "4");
		Questions pitanje5 = new Questions(new String[] {"3","5","4","6"},
				"Koliko NBA titula je osvojio Majkl Dzordan u toku svoje karijere?","6");
		Questions pitanje6 = new Questions(new String[] {"Silvester Stalone", "Klint Istvud", "Arnold Svarceneger", "Brus Vilis"}, 
				"Ko glumi glavnu ulogu u filmu Umri muski?", "Brus Vilis");
		Questions pitanje7 = new Questions(new String[] {"Vodozemce","Sisare","Gmizavce","Ribe"},
				"Zabe spadaju u: ", "Vodozemce");
		Questions pitanje8 = new Questions(new String[] {"1914","1912","1918","1916"},
				"Kada je poceo Prvi svetski rat?", "1914");
		Questions pitanje9 = new Questions(new String[] {"Nevada","Florida","Teksas","Kalifornija"},
				"Grad Majami se nalazi u americkoj drzavi: ", "Florida");
		Questions pitanje10 = new Questions(new String[] {"1000e", "500e", "100e", "2000e"},
				"Najveca Euro novcanica je: ", "500e");
		questionsList.add(pitanje1); questionsList.add(pitanje2); questionsList.add(pitanje3); questionsList.add(pitanje4);
		questionsList.add(pitanje5); questionsList.add(pitanje6); questionsList.add(pitanje7); questionsList.add(pitanje8);
		questionsList.add(pitanje9); questionsList.add(pitanje10);
	}
	public static String generateCode() {
		String code;
		while(true) {
			code = Integer.toString((new Random()).nextInt(90000000) + 10000000);
			if(!codesList.contains(code)) break;
		}
		return code;
	}
	
	public static void main(String[] args) {
		generateQuestionsList();
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
