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
	private WaitMonitor waiterPair;
	
	private String username;
	private boolean isQuit = false;
	private ClientHandler pair;
	private boolean isPaired = false;
	private String code;
	
	//ATRIBUTI ZA IGRU MOJ BROJ
	private MyNumbers myNumbers;
	private boolean isMojBrojPlayed = false;
	private boolean isMojBrojPlayedOfPair = false;
	private int mojBrojFinishedNumber;
	private int mojBrojFinishedNumberOfPair;
	//ATRIBUTI ZA IGRU KVIZ (KO ZNA ZNA)
	private Questions[] nizPitanja;
	private boolean isQuestionAnswered = false;
	private boolean isQuestionAnsweredOfPair = false;
	private String isCorrectAnswer;
	private String isCorrectAnswerOfPair;
	
	
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
		isQuit = hm;
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
	private void createNewWaiterPair() {
		Server.waitersPair.remove(waiterPair);
		waiterPair = new WaitMonitor(2);
		pair.waiterPair = waiterPair;
		Server.waitersPair.add(waiterPair);
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
	private void pairRandom() throws InterruptedException {
		synchronized(waiterPair) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && waiterPair == client.waiterPair) {				
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
			} else return;
		}
		clientOutput.println(pair.username);
	}
	private void pairByCode() throws IOException, InterruptedException {	
		for (ClientHandler client : Server.onlineUsers) {
			if(client != this && client.isPaired == false && code.equals(client.code)) {
				this.waiterPair = client.waiterPair;
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
		clientOutput.println(pair.username);
	}
	private void pairing() throws IOException, InterruptedException {
		String key = clientInput.readLine();
		switch (key) {
		//SLUCAJ NASUMICNOG POVEZIVANJA
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
		//SLUCAJ OTVARANJA SOBE I GENERISANJA KODA
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
		//SLUCAJ PRISTUPANJA SOBI PUTEM KODA
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
		writeMyNumbersJson();
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
		} else if(mojBrojFinishedNumber == Integer.MAX_VALUE && mojBrojFinishedNumberOfPair == Integer.MAX_VALUE) {
			clientOutput.println("Oba igraca bez bodova!");
		} else {
			clientOutput.println("Nereseno!");
		} 
	}
	private JSONArray initializeAnswersJSON(Questions pitanje) {
		JSONArray answers = new JSONArray();
		for (int i = 0; i < 4; i++) {
			answers.add(pitanje.getAnswer(i));
		}
		return answers;
	}
	private void writePitanja(Questions[] nizPitanja) {
		JSONObject quiz = new JSONObject();
		JSONArray questions = new JSONArray();
		for(int questionIndex = 0; questionIndex < 5; questionIndex++) {
			JSONObject question = new JSONObject();
			JSONArray answers = initializeAnswersJSON(nizPitanja[questionIndex]);
			question.put("answers", answers);
			question.put("question", nizPitanja[questionIndex].getQuestion());
			question.put("correctAnswer", nizPitanja[questionIndex].getCorrectAnswer());
			questions.add(question);
		}
		quiz.put("questions", questions);
		clientOutput.println(quiz.toJSONString());
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
	private void finish() {
		isQuit = true;
		if(Server.codesList.contains(code)) {
			Server.codesList.remove(code);
		}
		if(waiterPair != null) { 
			waiterPair.removeConnection();
		}
		//CUDNO, nekako iako izbrises iz waitersPair liste on ima drugog za dodeljivanje, super ali kako?
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
			pairing();
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
