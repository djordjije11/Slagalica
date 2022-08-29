package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

import quizClasses.Questions;

public class Server {
	public static LinkedList<ClientHandler> onlineUsers = new LinkedList<ClientHandler>();	//svi klijenti koji su se konektovali
	public static LinkedList<WaitMonitor> waitersPair = new LinkedList<WaitMonitor>();
	public static LinkedList<Questions> questionsList = new LinkedList<Questions>();	//baza svih pitanja za igru Kviz (Ko zna zna)
	public static LinkedList<String> codesList = new LinkedList<String>();	//svi generisani kodovi za otvaranje sobe i pristupanje njima koji su aktivni
	
	private static void generateQuestionsList() {
		Questions pitanje1 = new Questions(new String[]{"Scarface", "Dog Day Afternoon", "Mystic River", "Donnie Brasco"}, 
				"U kom od navedenih filmova ne glumi Al Pacino?", "Mystic River");
		Questions pitanje2 = new Questions(new String[] {"1789", "1889", "1389", "1689"},
				"Koje godine je pocela francuska revolucija?", "1789");
		Questions pitanje3 = new Questions(new String[]{"Borislav Pekic", "Lav Tolstoj", "Irving Stoun", "Herman Hese"}, 
				"Koji pisac je napisao knjigu \"Stepski vuk\"?", "Herman Hese");
		Questions pitanje4 = new Questions(new String[]{"5", "2", "4", "6"}, 
				"Za koliko razlicitih fudbalskih klubova je igrao Kristijano Ronaldo", "4");
		Questions pitanje5 = new Questions(new String[] {"3","5","4","6"},
				"Koliko NBA titula je osvojio Majkl Dzordan u toku svoje karijere?","6");
		Questions pitanje6 = new Questions(new String[] {"Silvester Stalone", "Klint Istvud", "Arnold Svarceneger", "Brus Vilis"}, 
				"Ko glumi glavnu ulogu u filmu \"Umri muski\"?", "Brus Vilis");
		Questions pitanje7 = new Questions(new String[] {"Vodozemce","Sisare","Gmizavce","Ribe"},
				"Zabe spadaju u: ", "Vodozemce");
		Questions pitanje8 = new Questions(new String[] {"1914","1912","1918","1916"},
				"Kada je poceo Prvi svetski rat?", "1914");
		Questions pitanje9 = new Questions(new String[] {"Nevada","Florida","Teksas","Kalifornija"},
				"Grad Majami se nalazi u americkoj drzavi: ", "Florida");
		Questions pitanje10 = new Questions(new String[] {"1000e", "500e", "100e", "2000e"},
				"Najveca Euro novcanica je: ", "500e");
		Questions pitanje11 = new Questions(new String[] {"velikog vezira", "pasaluk", "janjicara", "sultana"},
				"Beglerbegluk je u Osmanskom carstvu bio drugi naziv za: ", "pasaluk");
		Questions pitanje12 = new Questions(new String[] {"Models", "Hurricane", "Frajle", "Zana"},
				"Pevacica Marija Mirkovic je bivsa clanica grupe: ", "Frajle");
		Questions pitanje13 = new Questions(new String[] {"Uvac", "Rzav", "Djetinja", "Cetina"}, 
				"Koja reka protice kroz Uzice?", "Djetinja");
		Questions pitanje14 = new Questions(new String[] {"13.", "16.", "14.", "15."},
				"U kom veku je Kolumbo otkrio Ameriku?", "15.");
		Questions pitanje15 = new Questions(new String[] {"Baha", "Mocarta", "Betovena", "Cajkovskog"},
				"\"Meseceva sonata\" je delo kompozitora: ", "Betovena");
		Questions pitanje16 = new Questions(new String[] {"Kine", "Juzne Koreje", "Malezije", "Japana"},
				"Zlatnu medalju u stonom tenisu na Olimpijadi 2004. je osvojio Ryu Seung-Min iz: ", "Juzne Koreje");
		Questions pitanje17 = new Questions(new String[] {"Zemlja Nomada", "Belfast", "Parazit", "Koda"},
				"Oskar za najbolji film 2021. godine pripao je filmu: ", "Koda");
		Questions pitanje18 = new Questions(new String[] {"Zvucnicima", "Usima", "Termoakumulacionoj", "Ogledalu"},
				"Magnet se nalazi u: ", "Zvucnicima");
		Questions pitanje19 = new Questions(new String[] {"Aristotel", "Sokrat", "Platon", "Diogen"},
				"\"Ja znam samo jedno, a to je da nista ne znam\", rekao je: ", "Sokrat");
		Questions pitanje20 = new Questions(new String[] {"20.", "18.", "16.", "19."},
				"U kom veku je nastala muzika blues?", "19.");
		Questions pitanje21 = new Questions(new String[] {"Miroslav Ilic", "Toma Zdravkovic", "Haris Dzinovic", "Saban Saulic"},
				"Koji pevac narodne muzike se pojavljuje u filmu \"Bolje od bekstva\"?", "Toma Zdravkovic");
		Questions pitanje22 = new Questions(new String[] {"Lerkari Fridiju", "Salvatoru Rini", "Al Kaponeu", "Lakiju Lucanu"},
				"Pravo ime mu je Salvatore Lukanija. Bio je i osnivac mafijaske porodice Djenoveze. Rec je o: ", "Lakiju Lucanu");
		questionsList.add(pitanje1); questionsList.add(pitanje2); questionsList.add(pitanje3); questionsList.add(pitanje4);
		questionsList.add(pitanje5); questionsList.add(pitanje6); questionsList.add(pitanje7); questionsList.add(pitanje8);
		questionsList.add(pitanje9); questionsList.add(pitanje10); questionsList.add(pitanje11); questionsList.add(pitanje12);
		questionsList.add(pitanje13); questionsList.add(pitanje14); questionsList.add(pitanje15); questionsList.add(pitanje16);
		questionsList.add(pitanje17); questionsList.add(pitanje18); questionsList.add(pitanje19); questionsList.add(pitanje20);
		questionsList.add(pitanje21); questionsList.add(pitanje22);
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
		generateQuestionsList();		//generise se baza koja sadrzi sva moguca pitanja za igru Kviz (Ko zna zna)
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
					//na svaka dva nova klijenta pravi se nov objekat koji ce kasnije dodeliti dvojici spojenih igraca
					//i on ce sluziti da bi se niti sinhronizovale
				}
				socketCommunication = socket.accept();	//prihvata se konekcija od klijenta (igraca)
				connectionCounter++;	//ova promenljiva broji koliko se klijenata dosad pokrenulo, da bismo na svaka dva dodali waiterPair
				System.out.println("Konekcija je uspostavljena!");
				ClientHandler client = new ClientHandler(socketCommunication);
				client.start();	//pokrece se nova nit ClientHandler klase za svakog klijenta, kako bi ta nit sa serverske strane hendlovala klijenta
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
