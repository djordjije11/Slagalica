package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static PrintStream serverOutput;
	//private static BufferedReader console;
	
	private static Username usernameGUI;
	private static MojBroj mojbrojGUI;
	private static Quiz quizGUI;
	static WaitMonitor waiter;
	
	private static String username;
	private static String usernameOfPair;
	private static int scores = 0;
	private static int scoresOfPair = 0;
	
	
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
	private static Questions[] getQuestionsFromJSON(JSONObject object) {
		JSONArray questions = (JSONArray) object.get("questions");
		Questions[] pitanjaNiz = new Questions[4];
		for(int i = 0; i < 4; i++) {
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
	private static void setUsername() throws IOException {
		String input;
		usernameGUI = new Username(waiter);
		while(true) {
			synchronized(waiter) {
				try {
					waiter.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
		input = serverInput.readLine();
		usernameGUI.setPair(input);
		//input je ">>> Tvoj par je " + pair.username
		usernameOfPair = input.substring(16);
	}
	private static void startMojBroj() throws IOException {
		String input = serverInput.readLine();
		JSONObject object = parseJSON(input);
		if(object == null) {
			System.out.println("GRESKA");
			return;
		}
		MyNumbers myNumbers = getMyNumbersFromJSON(object);
		mojbrojGUI = new MojBroj(myNumbers, waiter, username, usernameOfPair, scores, scoresOfPair);
		//new Thread(mojbrojGUI).start();
		synchronized(waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		serverOutput.println(Integer.toString(mojbrojGUI.getFinishedNumber()));
		do {
			input = serverInput.readLine();
		} while (input == null);
		mojbrojGUI.setMessageLabel(input);
		scores = mojbrojGUI.getScores();
		scoresOfPair = mojbrojGUI.getScoresOfPair();
	}
	
	private static void startQuiz() throws IOException {
		String input = serverInput.readLine();
		JSONObject object = parseJSON(input);
		if(object == null) {
			System.out.println("GRESKA");
			return;
		}
		Questions[] pitanjaNiz = getQuestionsFromJSON(object);
		quizGUI = new Quiz(pitanjaNiz, waiter, username, usernameOfPair, scores, scoresOfPair);
		int i = 0;
		do {
			synchronized(waiter) {
				try {
					waiter.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			serverOutput.println(quizGUI.getIsCorrect());
			input = serverInput.readLine();
			quizGUI.setMessage(input);
			i++;
		} while (i < 4);
		scores = quizGUI.getScores();
		scoresOfPair = quizGUI.getScoresOfPair();
	}
	
	private static JSONObject parseJSON(String input) {
		JSONParser parser = new JSONParser();
		JSONObject object = null;
		try {
			object = (JSONObject) parser.parse(input);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return object;
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
			startMojBroj();
			startQuiz();
			socketCommunication.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
