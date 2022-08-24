package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mojbrojClasses.MyNumbers;
import quizClasses.Questions;

public class ClientHandler extends Thread {
	private BufferedReader clientInput = null;
	private PrintStream clientOutput = null;
	private Socket socketCommunication = null;
	
	private String username;
	public ClientHandler pair;
	public boolean isPaired = false;
	private boolean isQuit = false;
	private WaitMonitor waiterPair;
	private String code;
	
	public MyNumbers myNumbers;
	public Questions[] nizPitanja;
	
	public boolean isMojBrojPlayed = false;
	public boolean isMojBrojPlayedOfPair = false;
	public int mojBrojFinishedNumber;
	public int mojBrojFinishedNumberOfPair;
	
	public boolean isQuestionAnswered = false;
	public boolean isQuestionAnsweredOfPair = false;
	public String isCorrectAnswer;
	public String isCorrectAnswerOfPair;
	
	
	public void setIsQuit(boolean hm) {
		isQuit = hm;
	}
	public String getCode() { 
		return code;
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
	public WaitMonitor getWaiterPair() {
		return waiterPair;
	}
	public String getUsername() {
		return username;
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ClientHandler)) {
			return false;
		}
		ClientHandler client = (ClientHandler) obj;
		if(this.username.equals(client.getUsername())) {
			return true;
		} else return false;
	}
	ClientHandler(Socket socketCommunication) {
		this.socketCommunication = socketCommunication;
	}
	ClientHandler(String username){
		this.username = username;
	}
	private void createNewWaiterPair() {
		Server.waitersPair.remove(waiterPair);
		waiterPair = new WaitMonitor(2);
		pair.waiterPair = waiterPair;
		Server.waitersPair.add(waiterPair);
	}
	private void pairRandom() throws InterruptedException {
		synchronized(waiterPair) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && waiterPair == client.getWaiterPair()) {				
					client.pair = this;
					this.pair = client;
					pair.isPaired = true;
					this.isPaired = true;
					myNumbers = new MyNumbers();
					pair.myNumbers = this.myNumbers;
					nizPitanja = getRandomQuestions();
					pair.nizPitanja = this.nizPitanja;
					waiterPair.notify();
					/*
					 *
					 */
					break;
				}
			}
		}
		if(!isPaired) {
			ExitThread exit = new ExitThread(waiterPair, this, socketCommunication);
			exit.start();
			synchronized(waiterPair) {
				waiterPair.wait();
			}
			if(isQuit == false) {
				exit.interrupt();
			}
			else return;
		}
		clientOutput.println(pair.getUsername());
	}
	private void setUsername() throws IOException {
		String input;
		do {
			input = clientInput.readLine();
			if(Server.onlineUsers.contains(new ClientHandler(input))) {
				clientOutput.println("Uneti username je vec koriscen. Pokusaj opet!");
				continue;
			} else break;
		} while (true);
		username = input;
		Server.onlineUsers.add(this);
		clientOutput.println("Dobrodosli " + username + "!");
	}
	private void sendMessages() throws IOException {
		String message;
		while(true) {
			message = clientInput.readLine();
			if(message == null) return;
			if(message.equals("***quit")) {
				isQuit = true;
				this.clientOutput.println(">>> Dovidjenja!");
				for (ClientHandler client : Server.onlineUsers) {
					if(client == pair) {
						client.pair = null;
						client.clientOutput.println(">>> Doticni " + username + " nas je napustio.");
						client.clientOutput.println(">>> Ukoliko zelite da nadjete novu konekciju, ukucajte ***reset\nUkoliko zelite da izadjete, ukucajte ***quit");
						return;
					}
				}
			} else if(message.equals("***reset")){
				isPaired = false;
				return;
			} else {
				for (ClientHandler client : Server.onlineUsers) {
					if(client != null && client == pair) {
						client.clientOutput.println(username + ": " + message);
					}
				}
			}
		}
	}
	private void writeMyNumbersJson(String text) {
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
		writeMyNumbersJson(username + "_myNumbersJSON.json");
		String message = clientInput.readLine();
		if(message.equals("EXIT")) {
			setIsQuit(true);
			pair.setIsQuit(true);
			if(pair.isMojBrojPlayed) {
				synchronized(waiterPair) {
					waiterPair.notify();
				}
			}
			return;
		}
		if(isQuit) {
			clientOutput.println("Protivnik je napustio igru.");
			return;
		}
		//Primljen je mojBrojFinishedNumber iz igre Moj Broj
		mojBrojFinishedNumber = Integer.parseInt(message);
		isMojBrojPlayed = true;
		if(pair.isMojBrojPlayed) {
			synchronized(waiterPair) {
				pair.mojBrojFinishedNumberOfPair = mojBrojFinishedNumber;
				pair.isMojBrojPlayedOfPair = isMojBrojPlayed;
				mojBrojFinishedNumberOfPair = pair.mojBrojFinishedNumber;
				isMojBrojPlayedOfPair = pair.isMojBrojPlayed;
				waiterPair.notify();
			}
		} else {
			while(!isMojBrojPlayedOfPair) {
				synchronized(waiterPair) {
					waiterPair.wait();
				}
				if(isQuit) {
					clientOutput.println("Protivnik je napustio igru.");
					return;
				}
			}
		}
		if(mojBrojFinishedNumber < mojBrojFinishedNumberOfPair) {
			clientOutput.println("Pobedili ste!");
		} else if(mojBrojFinishedNumber > mojBrojFinishedNumberOfPair) {
			clientOutput.println("Izgubili ste!");
		} else {
			clientOutput.println("Nereseno!");
		}
	}
	private void startQuiz() throws IOException, InterruptedException {
		writePitanja(nizPitanja);
		String input;
		int i = 0;
		do {
			input = clientInput.readLine();
			if(input.equals("EXIT")) {
				setIsQuit(true);
				pair.setIsQuit(true);
				synchronized(waiterPair) {
					waiterPair.notify();
				}
				return;
			}
			if(isQuit) {
				clientOutput.println("Protivnik je napustio igru.");
				return;
			}
			isCorrectAnswer = input;
			isQuestionAnswered = true;
			if(pair.isQuestionAnswered) {
				synchronized(waiterPair) {
					pair.isCorrectAnswerOfPair = this.isCorrectAnswer;
					pair.isQuestionAnsweredOfPair = true;
					this.isCorrectAnswerOfPair = pair.isCorrectAnswer;
					waiterPair.notify();
				}
			} else {
				while(!isQuestionAnsweredOfPair) {
					synchronized(waiterPair) {
						waiterPair.wait();
					}
					if(isQuit) {
						clientOutput.println("Protivnik je napustio igru.");
						return;
					}
				}
			}
			String key = isCorrectAnswer + "-" + isCorrectAnswerOfPair;
			clientOutput.println(key);
			isQuestionAnswered = false; isCorrectAnswer = null; isQuestionAnsweredOfPair = false; isCorrectAnswerOfPair = null;
			i++;
		} while(i < 5);
	}
	private void writePitanja(Questions[] nizPitanja) {
		JSONObject quiz = new JSONObject();
		JSONArray questions = new JSONArray();
		JSONObject question1 = new JSONObject();
		JSONObject question2 = new JSONObject();
		JSONObject question3 = new JSONObject();
		JSONObject question4 = new JSONObject();
		JSONObject question5 = new JSONObject();
		JSONArray answers1 = initializeAnswersJSON(nizPitanja[0]);
		JSONArray answers2 = initializeAnswersJSON(nizPitanja[1]);
		JSONArray answers3 = initializeAnswersJSON(nizPitanja[2]);
		JSONArray answers4 = initializeAnswersJSON(nizPitanja[3]);
		JSONArray answers5 = initializeAnswersJSON(nizPitanja[4]);
		question1.put("answers", answers1);
		question2.put("answers", answers2);
		question3.put("answers", answers3);
		question4.put("answers", answers4);
		question5.put("answers", answers5);
		question1.put("question", nizPitanja[0].getQuestion());
		question2.put("question", nizPitanja[1].getQuestion());
		question3.put("question", nizPitanja[2].getQuestion());
		question4.put("question", nizPitanja[3].getQuestion());
		question5.put("question", nizPitanja[4].getQuestion());
		question1.put("correctAnswer", nizPitanja[0].getCorrectAnswer());
		question2.put("correctAnswer", nizPitanja[1].getCorrectAnswer());
		question3.put("correctAnswer", nizPitanja[2].getCorrectAnswer());
		question4.put("correctAnswer", nizPitanja[3].getCorrectAnswer());
		question5.put("correctAnswer", nizPitanja[4].getCorrectAnswer());
		questions.add(question1); questions.add(question2); questions.add(question3);
		questions.add(question4); questions.add(question5);
		quiz.put("questions", questions);
		clientOutput.println(quiz.toJSONString());
	}
	private JSONArray initializeAnswersJSON(Questions pitanje) {
		JSONArray answers = new JSONArray();
		for (int i = 0; i < 4; i++) {
			answers.add(pitanje.getAnswer(i));
		}
		return answers;
	}

	private void pairByCode() throws IOException, InterruptedException {	
		for (ClientHandler client : Server.onlineUsers) {
			if(client != this && client.isPaired == false && code.equals(client.getCode())) {
				this.waiterPair = client.getWaiterPair();
				waiterPair.addConnection();
				synchronized(waiterPair) {
					client.pair = this;
					this.pair = client;
					pair.isPaired = true;
					this.isPaired = true;
					myNumbers = new MyNumbers();
					pair.myNumbers = this.myNumbers;
					nizPitanja = getRandomQuestions();
					pair.nizPitanja = this.nizPitanja;
					waiterPair.notify();
					break;
				}
			}
		}
		clientOutput.println(pair.getUsername());
	}
	
	private void pairing() throws IOException, InterruptedException {
		String key = clientInput.readLine();
		switch (key) {
		case "R": {
			for (WaitMonitor waiter : Server.waitersPair) {
				if(waiter.getConnectionCounter() < 2) {
					waiterPair = waiter;
					waiterPair.addConnection();
					break;
				}
			}
			pairRandom();
			break;
		}
		case "G": {
			synchronized(Server.codesList) {
				code = Server.generateCode();
				Server.codesList.add(code);
			}
			clientOutput.println(code);
			waiterPair = new WaitMonitor(1);
			Server.waitersPair.add(waiterPair);
			while(!isPaired) {
				ExitThread exit = new ExitThread(waiterPair, this, socketCommunication);
				exit.start();
				synchronized(waiterPair) {
					waiterPair.wait();
				}
				if(isQuit == false) {
					exit.interrupt();
				}
				else return;
			}
			clientOutput.println(pair.username);
			break;
		}
		case "P": {
			while(true) {
				String input = clientInput.readLine();
				synchronized(Server.codesList) {
					if(!Server.codesList.contains(input)) {
						clientOutput.println("Uneli ste nepostojeci kod.");
						continue;
					} else {
						code = input;
						Server.codesList.remove(code);
						break;
					}
				}
			}
			pairByCode();
			break;
		}
		default:
			setIsQuit(true);
			return;
		}
	}
	
	private void finish() {
		isQuit = true;
		if(Server.codesList.contains(code)) {
			Server.codesList.remove(code);
		}
		if(waiterPair != null) { 
			waiterPair.removeConnection();
		}
		//CUDNO, nekako iako izbrises iz waitersPair liste on ima drugog za dodeljivanje, super al kako?
		if(Server.waitersPair.contains(waiterPair) && waiterPair.getConnectionCounter() == 0) {
			Server.waitersPair.remove(waiterPair);
		}
		waiterPair = null;
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
			if(!isPaired) {
				pairing();
			}
			if(!isQuit) {
				startMojBroj();
			}
			if(!isQuit) {
				startQuiz();
			}
			
			finish();
			socketCommunication.close();
			System.out.println("Konekcija zatvorena.");
		} catch (IOException e) {
			finish();
			System.out.println("Konekcija zatvorena.");
		} catch (InterruptedException e) {
			finish();
			e.printStackTrace();
		}
	}
}
