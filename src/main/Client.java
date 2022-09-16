package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
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
			serverOutput.println("finish ExitThread");	//closing the server's ExitThread
			return;
		}
		serverOutput.println("finish ExitThread");	//closing the server's ExitThread
	}
	private static void setUsername() throws IOException, InterruptedException {
		String input;
		usernameGUI = new Username(waiter);
		while(true) {
			synchronized(waiter) {
				waiter.wait();		//waiting for client to enter the username in the valid form
			}
			serverOutput.println(usernameGUI.getUsername());	//sending username to the server
			input = serverInput.readLine();
			usernameGUI.setMessage(input);
			if(input.equals("Uneti username je vec koriscen. Pokusaj opet!")) {
				continue;	//if the username is alreayd used, sending username to the server repeats
			} else break;
		}
		username = usernameGUI.getUsername();
	}
	private static void pairing() throws IOException, InterruptedException {
		pairingGUI = new Pairing(waiter, username);
		synchronized(waiter) {
			waiter.wait();	//waiting for client to choose an option
		}
		char key = pairingGUI.getMessage();
		serverOutput.println(key);	//sending chosen option to the server
		switch (key) {
		//"RANDOM PAIRING" OPTION
		case 'R': {
			String input = serverInput.readLine();	//gets from server the opponent's username
			if(input.startsWith("#")) {
				usernameOfPair = input.substring(1);
				pairingGUI.setPairLabel(usernameOfPair);
				serverOutput.println("finish ExitThread");	//closing the server's ExitThread
			} else {
				usernameOfPair = input;
				pairingGUI.setPairLabel(usernameOfPair);
			}
			return;
		}
		//"GENERATING CODE AND CREATING GAME ROOM" OPTION
		case 'G': {
			String code = serverInput.readLine();	//gets the generated code from server
			pairingGUI.setCode(code);
			usernameOfPair = serverInput.readLine();	//gets from server the opponent's username
			pairingGUI.setPairLabel(usernameOfPair);
			serverOutput.println("finish ExitThread");	//closing the server's ExitThread
			return;
		}
		//"ENTERING THE GAME ROOM BY USING THE CODE" OPTION
		case 'P': {
			String code;
			while(true) {
				synchronized(waiter) {
					waiter.wait();	//waiting for client to enter the code in the valid form
				}
				code = pairingGUI.getCode();
				serverOutput.println(code);	//sending entered code to the server to pair players
				String input = serverInput.readLine();
				if(input.equals("Uneli ste nepostojeci kod.")) {
					pairingGUI.repeatCode(input);
				} else {
					usernameOfPair = input;	//gets from server the opponent's username
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
		String input = serverInput.readLine();	//gets random generated letters
		slagalicaGUI = new Slova(input, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//game starts
		synchronized(waiter) {
			waiter.wait();	//waiting for player to finish the game
		}
		serverOutput.println(slagalicaGUI.getResult());	//sending player's result to the server
		input = serverInput.readLine();	//gets the outcome of the game
		slagalicaGUI.setMessageLabel(input); //setting scores based on the outcome or notifies the player opponent has quit
		checkIfExit(input);	if(isQuit) return;
		scores = slagalicaGUI.getScores();	//saving player's scores
		scoresOfPair = slagalicaGUI.getScoresOfPair();	//saving opponent's scores
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
		String input = serverInput.readLine();	//gets random generated numbers
		JSONObject object = parseJSON(input);
		MyNumbers myNumbers = getMyNumbersFromJSON(object);	//converting JSON to Java object
		mojbrojGUI = new MojBroj(myNumbers, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//game starts
		synchronized(waiter) {
			waiter.wait();	//waiting for player to finish the game
		}
		serverOutput.println(mojbrojGUI.getResult());	//sending player's result to the server
		input = serverInput.readLine();	//gets the outcome of the game
		mojbrojGUI.setMessageLabel(input); //setting scores based on the outcome or notifies the player opponent has quit
		checkIfExit(input);	if(isQuit) return;
		scores = mojbrojGUI.getScores();	//saving player's scores
		scoresOfPair = mojbrojGUI.getScoresOfPair();	//saving opponent's scores
	}
	private static Questions[] getQuestionsFromJSON(JSONObject object) {
		JSONArray questions = (JSONArray) object.get("questions");
		Questions[] questionsArray = new Questions[5];
		for(int i = 0; i < 5; i++) {
			JSONObject question = (JSONObject) questions.get(i);
			String pitanje = (String) question.get("question");
			String tacanOdgovor = (String) question.get("correctAnswer");
			JSONArray answers = (JSONArray) question.get("answers");
			String[] odgovori = new String[4];
			for(int j = 0; j < 4; j++) {
				odgovori[j] = (String) answers.get(j);
			}
			questionsArray[i] = new Questions(odgovori, pitanje, tacanOdgovor);
		}
		return questionsArray;
	}
	private static void startQuiz() throws IOException, InterruptedException, ParseException {
		String input = serverInput.readLine();	//gets random generated questions
		JSONObject object = parseJSON(input);
		Questions[] questionsArray = getQuestionsFromJSON(object);	//converting JSON to Java object
		quizGUI = new Quiz(questionsArray, waiter, username, usernameOfPair, scores, scoresOfPair, serverOutput);	//game starts
		int i = 0;
		do {
			synchronized(waiter) {
				waiter.wait();	//waiting for player to answer the question
			}
			serverOutput.println(quizGUI.getResult());	//sending player's result to the server
			input = serverInput.readLine();	//gets the outcome of the game
			quizGUI.setMessageLabel(input);	//setting scores based on the outcome or notifies the player opponent has quit
			checkIfExit(input); if(isQuit) break;
			i++;
		} while (i < 5);	//repeating five times because the game is consist of five different questions
		scores = quizGUI.getScores();	//saving player's scores
		scoresOfPair = quizGUI.getScoresOfPair();	//saving opponent's scores
	}
	
	
	private static void gameOver() throws InterruptedException {
		new GameOver(waiter, username, usernameOfPair, scores, scoresOfPair, !isQuit);
		synchronized(waiter) {
			waiter.wait();
		}
		isQuit = false;
		usernameGUI = null;
		pairingGUI = null;
		slagalicaGUI = null;
		mojbrojGUI = null;
		quizGUI = null;
		usernameOfPair = null;
		scores = 0;
		scoresOfPair = 0;
		serverOutput.println("Play again!");
		return;
	}
	
	public static void main(String[] args) {
		String ip = JOptionPane.showInputDialog("Unesite IP adresu servera.");
		int port = 9001;
		try {
			socketCommunication = new Socket(ip, port);
			serverInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			serverOutput = new PrintStream(socketCommunication.getOutputStream());
			waiter = new WaitMonitor();
			setUsername();
			while (true) {
				pairing();
				startSlagalica();
				if(!isQuit) {
					startMojBroj();
				}
				if(!isQuit) {
					startQuiz();
				}
				gameOver();
			}
			//socketCommunication.close();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Uneli ste nepoznatu IP adresu!", "GRESKA!", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}