package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import slagalicaClasses.Rec;
import mojbrojClasses.MyNumbers;
import quizClasses.Questions;

public class ClientHandler extends Thread {
	private BufferedReader clientInput = null;
	private PrintStream clientOutput = null;
	private Socket socketCommunication = null;
	private WaitMonitor waiterPair;
	
	private String username;
	private boolean isQuit = false;	//promenljiva koja odredjuje da li klijent zavrsava igru
	private boolean askToPlayAgain = true;	//true ako je protivnik napustio igru
	private ClientHandler pair;	//promenljiva koja referencira ClientHandler instancu koja predstavlja igraca koji je par ovom igracu (ovoj instanci)
	private boolean isPaired = false;	//promenljiva koja odredjuje da li je klijentu dodeljen njegov par (protivnik)
	private String code; //promenljiva koja predstavlja generisan kod sobe kojoj kojoj je igrac pristupio (ukoliko je preko koda)
	
	//ATRIBUTI ZA IGRU SLAGALICA
	private Rec rec;	//random slova koji se dodeljuju igracu u igri
	private boolean isSlagalicaPlayed = false;
	private boolean isSlagalicaPlayedOfPair = false;
	private String slagalicaFinishedWord;	//razlika izmedju dobijenog i trazenog broja ovog igraca u igri Moj Broj
	private String slagalicaFinishedWordOfPair;
	//ATRIBUTI ZA IGRU MOJ BROJ
	private MyNumbers myNumbers;	//random brojevi koji se dodeljuju igracu u igri
	private boolean isMojBrojPlayed = false;
	private boolean isMojBrojPlayedOfPair = false;
	private int mojBrojFinishedNumber;	//razlika izmedju dobijenog i trazenog broja ovog igraca u igri Moj Broj
	private int mojBrojFinishedNumberOfPair;
	//ATRIBUTI ZA IGRU KVIZ (KO ZNA ZNA)
	private Questions[] questionsArray;		//random pitanja koja se dodeljuju igracu u igri
	private boolean isQuestionAnswered = false;
	private boolean isQuestionAnsweredOfPair = false;
	private String isCorrectAnswer;
	private String isCorrectAnswerOfPair;
	//ATRIBUTI ZA IGRU ASOCIJACIJE
	//private JSONObject asocijacijaPolja;
	//private boolean indikator = false;
	
	ClientHandler(Socket socketCommunication) {
		this.socketCommunication = socketCommunication;
	}
	ClientHandler(String username){
		this.username = username;
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ClientHandler)) {
			return false;
		}
		ClientHandler client = (ClientHandler) obj;
		if(this.username.equals(client.username)) {
			return true;
		} else return false;
	}
	public void setIsQuit(boolean hm) {
		synchronized(this) {
			isQuit = hm;
		}
	}
	private Questions[] getRandomQuestions() {
		Questions[] pitanja = new Questions[5];
		LinkedList<Questions> questions = (LinkedList<Questions>) Server.questionsList.clone();
		for (int i = 0; i < pitanja.length; i++) {
			pitanja[i] = questions.get(new Random().nextInt(questions.size()));
			questions.remove(pitanja[i]);
		}
		return pitanja;
	}
	private void setUsername() throws IOException {
		String input;
		do {
			input = clientInput.readLine();	//cita se String koji je klijent uneo kao svoj username
			if(Server.onlineUsers.contains(new ClientHandler(input))) {
				clientOutput.println("Uneti username je vec koriscen. Pokusaj opet!");
				continue;
			} else break;
		} while (true);
		username = input;
		Server.onlineUsers.add(this);	//dodaje se listi aktivnih usera trenutni klijent
		clientOutput.println("Dobrodosli " + username + "!");
	}
	private void pairRandom() throws IOException, InterruptedException, ParseException {
		synchronized(waiterPair) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && waiterPair == client.waiterPair) {
					//kada se nadju dve razlicite instance koje nisu povezane i imaju isti WaitMonitor objekat, povezuju se
					client.pair = this;
					this.pair = client;
					pair.isPaired = true;
					this.isPaired = true;
					//dodeljuju se suparnicima ista slova, isti brojevi i ista pitanja za igre
					rec = new Rec();
					pair.rec = this.rec;
					myNumbers = new MyNumbers();
					pair.myNumbers = this.myNumbers;
					questionsArray = getRandomQuestions();
					pair.questionsArray = this.questionsArray;
					//asocijacijaPolja = ucitajRandomAsocijaciju();
					//pair.asocijacijaPolja = this.asocijacijaPolja;
					waiterPair.paired = true;
					waiterPair.notify();	//obavestava se instanca koja ceka da bude povezana, da je povezana
					break;
				}
			}
		}
		if(!isPaired) {
			ExitThread exit = new ExitThread(waiterPair, this, clientInput);
			exit.start();	//pokrece se nit koja osluskuje da li je klijent napustio igru
			synchronized(waiterPair) {
				waiterPair.wait();	//ova nit ce stajati dok ne primi obavestenje da je instanca povezana ili da je klijent napustio igru
			}
			if(isQuit) return;
			clientOutput.println("#" + pair.username);	//klijentu se salje username njegovog para, protivnika
			if(exit.isAlive()) exit.join();
		} else {
			clientOutput.println(pair.username);
		}
	}
	private void pairByCode() throws IOException, InterruptedException, ParseException {	
		synchronized(Server.onlineUsers) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && code.equals(client.code)) {
					//instanci se dodeljuje WaitMonitor objekat koji ima instanca koja ima i isti kod, kako bi se niti sinhronizovale
					this.waiterPair = client.waiterPair;
					waiterPair.addConnection();
					Server.waitersPair.add(waiterPair);
					synchronized(waiterPair) {
						client.pair = this;
						this.pair = client;
						pair.isPaired = true;
						this.isPaired = true;
						//dodeljuju se suparnicima ista slova, isti brojevi i ista pitanja za igre
						rec = new Rec();
						pair.rec = this.rec;
						myNumbers = new MyNumbers();
						pair.myNumbers = this.myNumbers;
						questionsArray = getRandomQuestions();
						pair.questionsArray = this.questionsArray;
						//asocijacijaPolja = ucitajRandomAsocijaciju();
						//pair.asocijacijaPolja = this.asocijacijaPolja;
						waiterPair.paired = true;
						waiterPair.notify();	//obavestava se instanca koja ceka da bude povezana, da je povezana
						code = null; client.code = null;	//kod kojim su klijenti povezani im vise nije potreban
						break;
					}
				}
			}
		}
		clientOutput.println(pair.username);	//klijentu se salje username njegovog para, protivnika
	}
	private void pairing() throws IOException, InterruptedException, ParseException {
		String key = clientInput.readLine();	//cita se od klijenta uneta opcija za povezivanje sa drugim igracem 
		switch (key) {
		//OPCIJA NASUMICNOG POVEZIVANJA
		case "R": {
			boolean skip = false;
			synchronized(Server.waitersPair) {
				for (WaitMonitor waiter : Server.waitersPair) {
					if(waiter.getConnectionCounter() == 1) {
						waiterPair = waiter;
						//ovoj instanci ClientHandler klase dodeljuje se WaitMonitor objekat koji nije zauzelo
						//vise od jedne instance ClientHandler klase kako bi se te dve instance sinhronizovale putem tog objekta
						waiterPair.addConnection();
						skip = true;
						break;
					}
				}
				if(!skip) {
					for (WaitMonitor waiter : Server.waitersPair) {
						if(waiter.getConnectionCounter() == 0) {
							waiterPair = waiter;
							//ovoj instanci ClientHandler klase dodeljuje se WaitMonitor objekat koji nije zauzelo
							//vise od jedne instance ClientHandler klase kako bi se te dve instance sinhronizovale putem tog objekta
							waiterPair.addConnection();
							break;
						}
					}
				}
			}
			pairRandom();	//pokrece se metoda koja vrsi "nasumicno povezivanje"	
			return;
		}
		//OPCIJA OTVARANJA SOBE I GENERISANJA KODA
		case "G": {
			synchronized(Server.codesList) {
				code = Server.generateCode();	//generise se random kod
				Server.codesList.add(code);	//izgenerisani kod se dodaje u listu gde se cuvaju svi trenutno aktivni kodovi
			}
			clientOutput.println(code);	//klijentu se salje izgenerisani kod
			waiterPair = new WaitMonitor(1);	//instanci se dodeljuje nov WaitMonitor objekat
			//preko kog ce se sinhronizovati instanca ClientHandler klase koja hendluje klijenta koji je pristupio sobi putem istog koda
			while(!isPaired) {
				ExitThread exit = new ExitThread(waiterPair, this, clientInput);
				exit.start();	//pokrece se nit koja osluskuje da li je klijent napustio igru
				synchronized(waiterPair) {
					waiterPair.wait();	//ova nit ce stajati dok ne primi obavestenje da je instanca povezana ili da je klijent napustio igru
				}
				if(isQuit) return;
				clientOutput.println(pair.username);	//klijentu se salje username njegovog para, protivnika
				if(exit.isAlive()) exit.join();
			}
			return;
		}
		//OPCIJA PRISTUPANJA SOBI PUTEM KODA
		case "P": {
			while(true) {
				String input = clientInput.readLine();	//cita se kod koji je uneo klijent
				synchronized(Server.codesList) {
					if(!Server.codesList.contains(input)) {
						clientOutput.println("Uneli ste nepostojeci kod.");
						continue;
					} else {
						code = input;
						Server.codesList.remove(code);	//kada se klijentima dodeli isti kod po kom ce se spojiti taj kod se brise iz baze kako se ne bi niko vise njim spojio
						break;
					}
				}
			}
			pairByCode();	//pokrece se metoda kojom se vrsi povezivanje putem koda
			return;
		}
		default:
			setIsQuit(true);
			askToPlayAgain = false;
			return;
		}
	}
	private void startSlagalica() throws IOException, InterruptedException {
		clientOutput.println(rec.getRec());	//klijentu se salju random izgenerisana slova za igru
		String message = clientInput.readLine();	//cita se poruka od klijenta u kojoj bi trebalo da se nalazi rezultat igre slagalica, tj. dobijena rec
		synchronized(this) {
			if(isQuit) {
				clientOutput.println("Protivnik je napustio igru.");	//klijent se obavestava da je protivnik napustio igru
				clientInput.readLine();
				return;
			}
		}
		slagalicaFinishedWord = message;
		isSlagalicaPlayed = true;
		ExitThread exit = new ExitThread(waiterPair, this, clientInput);
		exit.start();	//pokrece se nit koja osluskuje da li je klijent napustio igru
		if(pair.isSlagalicaPlayed) {
			synchronized(waiterPair) {
				pair.slagalicaFinishedWordOfPair = slagalicaFinishedWord;
				pair.isSlagalicaPlayedOfPair = isSlagalicaPlayed;
				slagalicaFinishedWordOfPair = pair.slagalicaFinishedWord;
				isSlagalicaPlayedOfPair = pair.isSlagalicaPlayed;
				waiterPair.notify();
				//instanca ce se naci u ovom slucaju ukoliko je njena odgovarajuca instanca Client klase druga zavrsila igru
				//zato obavestava svog para (tj. povezanu instancu ClientHandler klase, tj. pair (atribut ove klase)) da je i on zavrsio
			}
		} else {
			while(!isSlagalicaPlayedOfPair) {
				synchronized(waiterPair) {
					waiterPair.wait();	//ova nit ce stajati dok ne primi obavestenje da je protivnik isto zavrsio igru
				}
				if(isQuit) {
					if(exit.getIsExit()) {
						askToPlayAgain = false;
						return;
					} else {
						clientOutput.println("Protivnik je napustio igru.");
						if(exit.isAlive()) exit.join();
						return;
					}
				}
			}
		}
		//ClientHandler salje svom Clientu poruku o tome koji je ishod igre u zavisnosti od dobijenih rezultata igraca
		if(slagalicaFinishedWord.length() > slagalicaFinishedWordOfPair.length()) {
			clientOutput.println(slagalicaFinishedWord.length());
		} else if(slagalicaFinishedWord.length() < slagalicaFinishedWordOfPair.length()) {
			clientOutput.println((slagalicaFinishedWordOfPair.length()*-1));
		} else if(slagalicaFinishedWord.length() == 0 && slagalicaFinishedWordOfPair.length() == 0) {
			clientOutput.println("Oba igraca bez bodova!");
		} else {
			clientOutput.println((slagalicaFinishedWordOfPair.length()+100));
		}
		if(exit.isAlive()) exit.join();
	}
	private void writeMyNumbersJson() {
		JSONObject objectJson = new JSONObject();
		JSONArray arrayJson = new JSONArray();
		for (int i = 0; i < myNumbers.getBrojeviLength(); i++) {
			arrayJson.add(myNumbers.getBroj(i));
		}
		objectJson.put("brojevi", arrayJson);
		objectJson.put("srednjiBroj", myNumbers.getSrednjiBroj());
		objectJson.put("veciBroj", myNumbers.getVeciBroj());
		objectJson.put("wantedNumber", myNumbers.getWantedNumber());
		clientOutput.println(objectJson.toJSONString());
	}
	private void startMojBroj() throws IOException, InterruptedException {
		writeMyNumbersJson();	//klijentu se salju random izgenerisani brojevi za igru
		String message = clientInput.readLine();	//cita se poruka od klijenta u kojoj bi trebalo da se nalazi rezultat igre moj broj, tj. razlika izmedju trazenog i dobijenog broja
		synchronized(this) {
			if(isQuit) {
				clientOutput.println("Protivnik je napustio igru.");	//klijent se obavestava da je protivnik napustio igru
				clientInput.readLine();
				return;
			}
		}
		mojBrojFinishedNumber = Integer.parseInt(message);
		isMojBrojPlayed = true;
		ExitThread exit = new ExitThread(waiterPair, this, clientInput);
		exit.start();	//pokrece se nit koja osluskuje da li je klijent napustio igru
		if(pair.isMojBrojPlayed) {
			synchronized(waiterPair) {
				pair.mojBrojFinishedNumberOfPair = mojBrojFinishedNumber;
				pair.isMojBrojPlayedOfPair = isMojBrojPlayed;
				mojBrojFinishedNumberOfPair = pair.mojBrojFinishedNumber;
				isMojBrojPlayedOfPair = pair.isMojBrojPlayed;
				waiterPair.notify();
				//instanca ce se naci u ovom slucaju ukoliko je njena odgovarajuca instanca Client klase druga zavrsila igru
				//zato obavestava svog para (tj. povezanu instancu ClientHandler klase, tj. pair (atribut ove klase)) da je i on zavrsio
			}
		} else {
			while(!isMojBrojPlayedOfPair) {
				synchronized(waiterPair) {
					waiterPair.wait();	//ova nit ce stajati dok ne primi obavestenje da je protivnik isto zavrsio igru
				}
				if(isQuit) {
					if(exit.getIsExit()) {
						askToPlayAgain = false;
						return;
					} else {
						clientOutput.println("Protivnik je napustio igru.");
						if(exit.isAlive()) exit.join();
						return;
					}
				}
			}
		}
		//ClientHandler salje svom Clientu poruku o tome koji je ishod igre u zavisnosti od dobijenih rezultata igraca
		if(mojBrojFinishedNumber < mojBrojFinishedNumberOfPair) {
			clientOutput.println("Pobedili ste!");
		} else if(mojBrojFinishedNumber > mojBrojFinishedNumberOfPair) {
			clientOutput.println("Izgubili ste!");
		} else if(mojBrojFinishedNumber == Integer.MAX_VALUE && mojBrojFinishedNumberOfPair == Integer.MAX_VALUE) {
			clientOutput.println("Oba igraca bez bodova!");
		} else {
			clientOutput.println("Nereseno!");
		}
		if(exit.isAlive()) exit.join();
	}
	private JSONArray initializeAnswersJSON(Questions pitanje) {
		JSONArray answers = new JSONArray();
		for (int i = 0; i < 4; i++) {
			answers.add(pitanje.getAnswer(i));
		}
		return answers;
	}
	private void writePitanja(Questions[] questionsArray) {
		JSONObject quiz = new JSONObject();
		JSONArray questions = new JSONArray();
		for(int questionIndex = 0; questionIndex < 5; questionIndex++) {
			JSONObject question = new JSONObject();
			JSONArray answers = initializeAnswersJSON(questionsArray[questionIndex]);
			question.put("answers", answers);
			question.put("question", questionsArray[questionIndex].getQuestion());
			question.put("correctAnswer", questionsArray[questionIndex].getCorrectAnswer());
			questions.add(question);
		}
		quiz.put("questions", questions);
		clientOutput.println(quiz.toJSONString());
	}
	private void startQuiz() throws IOException, InterruptedException {
		writePitanja(questionsArray);	//klijentu se salju random izgenerisana pitanja za igru
		String input;
		int i = 0;
		do {
			input = clientInput.readLine();	//cita se od klijenta poruka da li je odgovorio tacno ili mozda napustio igru
			synchronized(this) {
				if(isQuit) {
					clientOutput.println("Protivnik je napustio igru.");	//klijent se obavestava da je protivnik napustio igru
					clientInput.readLine();
					return;
				}
			}
			isCorrectAnswer = input;
			isQuestionAnswered = true;
			ExitThread exit = new ExitThread(waiterPair, this, clientInput);
			exit.start();	//pokrece se nit koja osluskuje da li je klijent napustio igru
			if(pair.isQuestionAnswered) {
				synchronized(waiterPair) {
					pair.isCorrectAnswerOfPair = this.isCorrectAnswer;
					pair.isQuestionAnsweredOfPair = true;
					this.isCorrectAnswerOfPair = pair.isCorrectAnswer;
					waiterPair.notify();	//obavestava se protivnik da je klijent odigrao potez
				}
			} else {
				while(!isQuestionAnsweredOfPair) {
					synchronized(waiterPair) {
						waiterPair.wait();	//ova nit ce stajati dok ne primi obavestenje da je protivnik isto zavrsio igru
					}
					if(isQuit) {
						if(exit.getIsExit()) {
							askToPlayAgain = false;
							return;
						} else {
							clientOutput.println("Protivnik je napustio igru.");
							if(exit.isAlive()) exit.join();
							return;
						}
					}
				}
			}
			String key = isCorrectAnswer + "-" + isCorrectAnswerOfPair;
			clientOutput.println(key);	//klijentu se salje ishod poteza u zavisnosti od odgovora igraca
			if(exit.isAlive()) exit.join();
			isQuestionAnswered = false; isCorrectAnswer = null; isQuestionAnsweredOfPair = false; isCorrectAnswerOfPair = null;
			i++;
		} while(i < 5);	//ponavlja se 5 puta jer igra sadrzi 5 pitanja, dakle 5 poteza
	}
	private JSONObject ucitajRandomAsocijaciju() throws IOException, ParseException {
		JSONParser jsonparser = new JSONParser();
		JSONObject randomAsocijacija = null;
		FileReader fr = new FileReader("baza\\asocijacije.json");
		JSONObject jsonObjekat = (JSONObject) jsonparser.parse(fr);
		JSONArray nizAsocijacije = (JSONArray) jsonObjekat.get("Asocijacije");
		randomAsocijacija = (JSONObject) nizAsocijacije.get(new Random().nextInt(nizAsocijacije.size()));
		fr.close();
		return randomAsocijacija;
	}
	
	private void pairQuit() {
		if(pair != null) pair.setIsQuit(true);
	}
	private void waiterPairEnd() {
		if(waiterPair != null) { 
			waiterPair.removeConnection();
			waiterPair.paired = false;
			waiterPair = null;
		}
	}
	private void isPlayAgain() throws IOException {
		if(clientInput.readLine().equals("Play again!")) {
			askToPlayAgain = true;
			isQuit = false;
			pair = null;
			isPaired = false;
			code = null;
			rec = null;
			isSlagalicaPlayed = false;
			isSlagalicaPlayedOfPair = false;
			slagalicaFinishedWord = null;
			slagalicaFinishedWordOfPair = null;
			myNumbers = null;
			isMojBrojPlayed = false;
			isMojBrojPlayedOfPair = false;
			mojBrojFinishedNumber = 0;
			mojBrojFinishedNumberOfPair = 0;
			questionsArray = null;
			isQuestionAnswered = false;
			isQuestionAnsweredOfPair = false;
			isCorrectAnswer = null;
			isCorrectAnswerOfPair = null;
		}
	}
	private void finish() {
		isQuit = true;
		synchronized(Server.waitersPair) {
			if(Server.waitersPair.contains(waiterPair) && waiterPair.getConnectionCounter() == 0) {
				Server.waitersPair.remove(waiterPair);
			}
		}
		if(code != null) {
			synchronized(Server.codesList) {
				if(Server.codesList.contains(code)) {
					Server.codesList.remove(code);
				}
			}
			code = null;
		}
		if(username != null) {
			Server.onlineUsers.remove(this);
		}
	}
	
	@Override
	public void run() {
		try {
			clientInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			clientOutput = new PrintStream(socketCommunication.getOutputStream());
			
			setUsername();
			while(!isQuit) {
				try {
					pairing();
				} catch (ParseException e) {
					setIsQuit(true);
				}
				if(!isQuit) {
					startSlagalica();
				}
				if(!isQuit) {
					startMojBroj();
				}
				if(!isQuit) {
					startQuiz();
				}
				pairQuit();
				waiterPairEnd();
				if(askToPlayAgain) isPlayAgain();
			}
			finish();
			socketCommunication.close();
			System.out.println("Konekcija zatvorena.");
		} catch (IOException e) {
			pairQuit();
			if(waiterPair != null && waiterPair.paired) {
				synchronized(waiterPair) {
					waiterPair.notify();
				}
			}
			waiterPairEnd();
			finish();
			System.out.println("Konekcija zatvorena.");
		} catch (InterruptedException e) {
			finish();
			System.out.println("Konekcija zatvorena.");
		} catch (Exception e) {
			finish();
			System.out.println("Konekcija zatvorena.");
		}
	}
}