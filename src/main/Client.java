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
	//private static BufferedReader console;
	
	private static WaitMonitor waiter;
	private static boolean isQuit = false;
	
	private static Username usernameGUI;
	private static Pairing pairingGUI;
	private static MojBroj mojbrojGUI;
	private static Quiz quizGUI;
	
	private static String username;
	private static String usernameOfPair;
	private static int scores = 0;
	private static int scoresOfPair = 0;
	
	private static void setUsername() throws IOException, InterruptedException {
		String input;
		usernameGUI = new Username(waiter);
		while(true) {
			synchronized(waiter) {
				waiter.wait();
			}
			serverOutput.println(usernameGUI.getUsername());
			input = serverInput.readLine();
			if(input == null) return;
			usernameGUI.setMessage(input);
			if(input.equals("Uneti username je vec koriscen. Pokusaj opet!")) {
				continue;
			} else break;
		}
		username = usernameGUI.getUsername();
	}
	private static void pair() throws IOException, InterruptedException {
		pairingGUI = new Pairing(waiter, username, serverOutput);
		synchronized(waiter) {
			waiter.wait();
		}
		char key = pairingGUI.getMessage(); 
		serverOutput.println(key);
		switch (key) {
		//SLUCAJ NASUMICNOG POVEZIVANJA
		case 'R': {
			usernameOfPair = serverInput.readLine();
			pairingGUI.setPairLabel(usernameOfPair);
			break;
		}
		//SLUCAJ OTVARANJA SOBE I GENERISANJA KODA
		case 'G': {
			String code = serverInput.readLine();
			pairingGUI.setCode(code);
			usernameOfPair = serverInput.readLine();
			pairingGUI.setPairLabel(usernameOfPair);
			break;
		}
		//SLUCAJ PRISTUPANJA SOBI PUTEM KODA
		case 'P': {
			String code;
			while(true) {
				synchronized(waiter) {
					waiter.wait();
				}
				code = pairingGUI.getCode();
				serverOutput.println(code);
				String input = serverInput.readLine();
				if(input.equals("Uneli ste nepostojeci kod.")) {
					pairingGUI.repeatCode(input);
				} else {
					usernameOfPair = input;
					pairingGUI.setPairLabel(usernameOfPair);
					break;
				}
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + key);
		}
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
		String input = serverInput.readLine();
		JSONObject object = parseJSON(input);
		MyNumbers myNumbers = getMyNumbersFromJSON(object);
		mojbrojGUI = new MojBroj(myNumbers, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);
		synchronized(waiter) {
			waiter.wait();
		}
		serverOutput.println(Integer.toString(mojbrojGUI.getFinishedNumber()));
		input = serverInput.readLine();
		mojbrojGUI.setMessageLabel(input);
		if(input.equals("Protivnik je napustio igru.")) {
			isQuit = true;
			return;
		}
		scores = mojbrojGUI.getScores();
		scoresOfPair = mojbrojGUI.getScoresOfPair();
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
		String input = serverInput.readLine();
		JSONObject object = parseJSON(input);
		Questions[] pitanjaNiz = getQuestionsFromJSON(object);
		quizGUI = new Quiz(pitanjaNiz, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);
		int i = 0;
		do {
			synchronized(waiter) {
				waiter.wait();
			}
			serverOutput.println(quizGUI.getIsCorrect());
			input = serverInput.readLine();
			quizGUI.setMessage(input);
			if(input.equals("Protivnik je napustio igru.")) {
				isQuit = true;
				return;
			}
			i++;
		} while (i < 5);
		scores = quizGUI.getScores();
		scoresOfPair = quizGUI.getScoresOfPair();
	}
	
	public static void main(String[] args) {
		//String ip = "192.168.0.18";
		String ip = "localhost";
		int port = 9001;
		try {
			socketCommunication = new Socket(ip, port);
			serverInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			serverOutput = new PrintStream(socketCommunication.getOutputStream());
			//console = new BufferedReader(new InputStreamReader(System.in));
			
			waiter = new WaitMonitor();
			
			setUsername();
			pair();
			startMojBroj();
			if(!isQuit) {
				startQuiz();
			}
			
			socketCommunication.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
