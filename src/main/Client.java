package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import guiClasses.*;
import mojbrojClasses.MyNumbers;
import quizClasses.Questions;

public class Client {
	private static Socket socketCommunication;
	private static BufferedReader serverInput;
	public static PrintStream serverOutput;
	private static WaitMonitor waiter;
	private static boolean isQuit = false;
	
	private static Username usernameGUI;
	private static Pairing pairingGUI;
	private static Slova slagalicaGUI;
	private static MojBroj mojbrojGUI;
	private static Quiz quizGUI;
	
	private static String username;
	private static String usernameOfPair;
	private static int scores = 0;
	private static int scoresOfPair = 0;
	
	
	private static void checkIfExit(String input) {
		if(input.equals("Protivnik je napustio igru.")) {
			isQuit = true;
			serverOutput.println("finish ExitThread");
			return;
		}
		serverOutput.println("finish ExitThread");
	}
	private static void setUsername() throws IOException, InterruptedException {
		String input;
		usernameGUI = new Username(waiter);
		while(true) {
			synchronized(waiter) {
				waiter.wait();		//ova nit ce stajati dok klijent ne unese username u adekvatnom obliku
			}
			serverOutput.println(usernameGUI.getUsername());	//salje se serveru username da bi proverio da li je vec neki klijent ulogovan sa tim usernameom
			input = serverInput.readLine();	//cita povratnu informaciju od servera
			if(input == null) return;
			usernameGUI.setMessage(input);	//prenosi se i na GUI dobijena poruka
			if(input.equals("Uneti username je vec koriscen. Pokusaj opet!")) {
				continue;
			} else break;
		}
		username = usernameGUI.getUsername();	//ukoliko je prosao sve provere, username se dodeljuje klijentu
	}
	private static void pair() throws IOException, InterruptedException {
		pairingGUI = new Pairing(waiter, username, serverOutput);
		synchronized(waiter) {
			waiter.wait();	//ova nit ce stajati dok klijent ne odabere opciju za povezivanje sa drugim igracem
		}
		char key = pairingGUI.getMessage();	//uzima se uneta opcija
		serverOutput.println(key);	//salje se klijentu uneta opcija za povezivanje sa drugim igracem
		switch (key) {
		//OPCIJA NASUMICNOG POVEZIVANJA
		case 'R': {
			String input = serverInput.readLine();	//cita se od servera username protivnika koji je ovom igracu dodeljen
			if(input.startsWith("#")) {
				usernameOfPair = input.substring(1);
				pairingGUI.setPairLabel(usernameOfPair);
				serverOutput.println("finish ExitThread");
			} else {
				usernameOfPair = input;
				pairingGUI.setPairLabel(usernameOfPair);
			}
			return;
		}
		//OPCIJA OTVARANJA SOBE I GENERISANJA KODA
		case 'G': {
			String code = serverInput.readLine();	//od servera se dobija generisan kod
			pairingGUI.setCode(code);
			usernameOfPair = serverInput.readLine();	//cita se od servera username protivnika koji je ovom igracu dodeljen
			pairingGUI.setPairLabel(usernameOfPair);
			serverOutput.println("finish ExitThread");
			return;
		}
		//OPCIJA PRISTUPANJA SOBI PUTEM KODA
		case 'P': {
			String code;
			while(true) {
				synchronized(waiter) {
					waiter.wait();	//ova nit ce stajati dok klijent ne unese kod u adekvatnom obliku
				}
				code = pairingGUI.getCode();
				serverOutput.println(code);	//serveru se salje kod da bi se pronasao igrac koji je izgenerisao taj kod
				String input = serverInput.readLine();	//cita se od servera povratna informacija
				if(input.equals("Uneli ste nepostojeci kod.")) {
					pairingGUI.repeatCode(input);
				} else {
					usernameOfPair = input;	//cita se od servera username protivnika koji je ovom igracu dodeljen
					pairingGUI.setPairLabel(usernameOfPair);
					break;
				}
			}
			return;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + key);
		}
	}
	private static void startSlagalica() throws IOException, InterruptedException, ParseException {
		String input = serverInput.readLine();	//cita se od servera objekat koji predstavlja brojeve koji su random dodeljeni igracu
		slagalicaGUI = new Slova(input, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//otvara se gui
		synchronized(waiter) {
			waiter.wait();	//ova nit ce stajati dok igrac ne zavrsi igru
		}
		serverOutput.println(slagalicaGUI.getFinishedRec());	//serveru se salje rezultat igraca, tj razlika izmedju trazenog broja i dobijenog
		input = serverInput.readLine();	//cita se povratna informacija od servera o tome kako je i protivnik odigrao u odnosu na igraca
		slagalicaGUI.setMessageLabel(input);	//na osnovu serverove poruke dodeljuju se bodovi igracima
		checkIfExit(input);	if(isQuit) return;
		scores = slagalicaGUI.getScores();	//cuvaju se bodovi igraca sa kraja igre kako bi se preneli u sledecu igru
		scoresOfPair = slagalicaGUI.getScoresOfPair();	//cuvaju se bodovi protivnika sa kraja igre kako bi se preneli u sledecu igru
	}
	private static JSONObject parseJSON(String input) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject object = null;
		object = (JSONObject) parser.parse(input);
		return object;
	}
	private static MyNumbers getMyNumbersFromJSON(JSONObject object) {
		JSONArray array = (JSONArray) object.get("brojevi");
		int[] brojevi = new int[4];
		for (int i = 0; i < 4; i++) {
			brojevi[i] = (int) ((long)(array.get(i)));
		}
		int srednjiBroj = (int) ((long)object.get("srednjiBroj"));
		int veciBroj = (int) ((long)object.get("veciBroj"));
		int wantedNumber = (int) ((long)object.get("wantedNumber"));
		return new MyNumbers(brojevi, srednjiBroj, veciBroj, wantedNumber);
	}
	private static void startMojBroj() throws IOException, InterruptedException, ParseException {
		String input = serverInput.readLine();	//cita se od servera objekat koji predstavlja brojeve koji su random dodeljeni igracu
		JSONObject object = parseJSON(input);
		MyNumbers myNumbers = getMyNumbersFromJSON(object);	//izvrsena je transformacija objekta iz json formata u java
		mojbrojGUI = new MojBroj(myNumbers, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//otvara se gui
		synchronized(waiter) {
			waiter.wait();	//ova nit ce stajati dok igrac ne zavrsi igru
		}
		serverOutput.println(Integer.toString(mojbrojGUI.getFinishedNumber()));	//serveru se salje rezultat igraca, tj razlika izmedju trazenog broja i dobijenog
		input = serverInput.readLine();	//cita se povratna informacija od servera o tome kako je i protivnik odigrao u odnosu na igraca
		mojbrojGUI.setMessageLabel(input);	//na osnovu serverove poruke dodeljuju se bodovi igracima
		checkIfExit(input);	if(isQuit) return;
		scores = mojbrojGUI.getScores();	//cuvaju se bodovi igraca sa kraja igre kako bi se preneli u sledecu igru
		scoresOfPair = mojbrojGUI.getScoresOfPair();	//cuvaju se bodovi protivnika sa kraja igre kako bi se preneli u sledecu igru
	}
	private static Questions[] getQuestionsFromJSON(JSONObject object) {
		JSONArray questions = (JSONArray) object.get("questions");
		Questions[] pitanjaNiz = new Questions[5];
		for(int i = 0; i < 5; i++) {
			JSONObject question = (JSONObject) questions.get(i);
			String pitanje = (String) question.get("question");
			String tacanOdgovor = (String) question.get("correctAnswer");
			JSONArray answers = (JSONArray) question.get("answers");
			String[] odgovori = new String[4];
			for(int j = 0; j < 4; j++) {
				odgovori[j] = (String) answers.get(j);
			}
			pitanjaNiz[i] = new Questions(odgovori, pitanje, tacanOdgovor);
		}
		return pitanjaNiz;
	}
	private static void startQuiz() throws IOException, InterruptedException, ParseException {
		String input = serverInput.readLine();	//cita se od servera objekat koji predstavlja pitanja koja su random dodeljena igracu
		JSONObject object = parseJSON(input);
		Questions[] pitanjaNiz = getQuestionsFromJSON(object);	//izvrsena je transformacija objekta iz json formata u java
		quizGUI = new Quiz(pitanjaNiz, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//otvara se gui
		int i = 0;
		do {
			synchronized(waiter) {
				waiter.wait();	//ova nit ce stajati dok igraca ne odigra potez (ne da odgovor na pitanje ili preskoci)
			}
			serverOutput.println(quizGUI.getIsCorrect());	//serveru se salje da li je igrac odgovorio tacno na pitanje ili ne, ili ga je preskocio
			input = serverInput.readLine();	//cita se povratna informacija od servera o tome kako je i protivnik odigrao u odnosu na igraca
			quizGUI.setMessage(input);	//na osnovu serverove poruke dodeljuju se bodovi igracima
			checkIfExit(input); if(isQuit) break;
			i++;
		} while (i < 5);	//ponavlja se 5 puta jer igra sadrzi 5 pitanja, dakle 5 poteza
		scores = quizGUI.getScores();	//cuvaju se bodovi igraca sa kraja igre kako bi se preneli u sledecu igru
		scoresOfPair = quizGUI.getScoresOfPair();	//cuvaju se bodovi protivnika sa kraja igre kako bi se preneli u sledecu igru
	}
	
	
	private static void gameOver() {
		new GameOver(username, usernameOfPair, scores, scoresOfPair);
	}
	
	public static void main(String[] args) {
		//String ip = "192.168.0.18";
		String ip = "localhost";
		int port = 9001;
		try {
			socketCommunication = new Socket(ip, port);
			serverInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			serverOutput = new PrintStream(socketCommunication.getOutputStream());
			waiter = new WaitMonitor();
			setUsername();	//klijent unosi username
			pair();	//klijentu se dodeljuje drugi klijent, par
			startSlagalica();
			if(!isQuit) {
				startMojBroj();	//zapocinje se igra Moj Broj
			}
			if(!isQuit) {
				startQuiz();	//zapocinje se igra Kviz (Ko zna zna)
			}
			if(!isQuit) {
				gameOver();
			}
			socketCommunication.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
