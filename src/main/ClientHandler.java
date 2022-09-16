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
	private boolean isQuit = false;	//did player or opponent quit the game
	private boolean askToPlayAgain = true;	//true if an opponent has left the game
	private ClientHandler pair;	//represents the instance of a ClientHandler class that handles opponent's instance of a Client class
	private boolean isPaired = false;
	private String code; //generated code for the game room
	
	//ATTRIBUTES FOR THE GAME SLAGALICA
	private Rec rec;	//random generated letters
	private boolean isSlagalicaPlayed = false;
	private boolean isSlagalicaPlayedOfPair = false;
	private String slagalicaFinishedWord;
	private String slagalicaFinishedWordOfPair;
	//ATTRIBUTES FOR THE GAME MOJ BROJ
	private MyNumbers myNumbers;	//random generated numbers
	private boolean isMojBrojPlayed = false;
	private boolean isMojBrojPlayedOfPair = false;
	private int mojBrojFinishedNumber;	//difference between calculated number and wanted number
	private int mojBrojFinishedNumberOfPair;
	//ATTRIBUTES FOR THE GAME KVIZ (KO ZNA ZNA)
	private Questions[] questionsArray;		//random generatedd questions
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
		synchronized(this) {
			isQuit = hm;
		}
	}
	private Questions[] getRandomQuestions() {
		Questions[] questions = new Questions[5];
		LinkedList<Questions> questionsListClone = (LinkedList<Questions>) Server.questionsList.clone();
		for (int i = 0; i < questions.length; i++) {
			questions[i] = questionsListClone.get(new Random().nextInt(questionsListClone.size()));
			questionsListClone.remove(questions[i]);
		}
		return questions;
	}
	private void setUsername() throws IOException {
		String input;
		do {
			input = clientInput.readLine();	//gets the string that client entered
			if(Server.onlineUsers.contains(new ClientHandler(input))) {
				clientOutput.println("Uneti username je vec koriscen. Pokusaj opet!");	// there cannot be two players in the system with the same username
				continue;
			} else break;
		} while (true);
		username = input;
		Server.onlineUsers.add(this);
		clientOutput.println("Dobrodosli " + username + "!");
	}
	private void pairRandom() throws IOException, InterruptedException, ParseException {
		synchronized(waiterPair) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && waiterPair == client.waiterPair) {
					//when two different instances that are not paired and have the same WaitMonitor instance are found, they are being paired
					client.pair = this;
					this.pair = client;
					pair.isPaired = true;
					this.isPaired = true;
					//the opponents are supplied with the same letters, the same numbers and the same game questions
					rec = new Rec();
					pair.rec = this.rec;
					myNumbers = new MyNumbers();
					pair.myNumbers = this.myNumbers;
					questionsArray = getRandomQuestions();
					pair.questionsArray = this.questionsArray;
					waiterPair.paired = true;
					waiterPair.notify();	//the opponent's ClientHandler instance waiting to be connected is notified that it is connected
					break;
				}
			}
		}
		if(!isPaired) {
			ExitThread exit = new ExitThread(waiterPair, this, clientInput);
			exit.start();	//the thread that will notify if client has quit
			synchronized(waiterPair) {
				waiterPair.wait();	//waiting for a notification that the opponents are paired or the client has quit
			}
			if(isQuit) return;
			clientOutput.println("#" + pair.username);	//sending pair's username to the client and message that ExitThread needs to be closed
			if(exit.isAlive()) exit.join();
		} else {
			clientOutput.println(pair.username);	//sending pair's username to the client
		}
	}
	private void pairByCode() throws IOException, InterruptedException, ParseException {	
		synchronized(Server.onlineUsers) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false && code.equals(client.code)) {
					//two ClientHandler instances with the same code get the same WaitMonitor instance, in order to keep the threads synchronized
					this.waiterPair = client.waiterPair;
					waiterPair.addConnection();
					Server.waitersPair.add(waiterPair);
					synchronized(waiterPair) {
						client.pair = this;
						this.pair = client;
						pair.isPaired = true;
						this.isPaired = true;
						//the opponents are supplied with the same letters, the same numbers and the same game questions
						rec = new Rec();
						pair.rec = this.rec;
						myNumbers = new MyNumbers();
						pair.myNumbers = this.myNumbers;
						questionsArray = getRandomQuestions();
						pair.questionsArray = this.questionsArray;
						waiterPair.paired = true;
						waiterPair.notify();	//the opponent's ClientHandler instance waiting to be connected is notified that it is connected
						code = null; client.code = null;	//the code is not useful anymore
						break;
					}
				}
			}
		}
		clientOutput.println(pair.username);	//sending pair's username to the client
	}
	private void pairing() throws IOException, InterruptedException, ParseException {
		String key = clientInput.readLine();	//gets the option that the client has chosen
		switch (key) {
		//"RANDOM PAIRING" OPTION
		case "R": {
			boolean skip = false;
			synchronized(Server.waitersPair) {
				for (WaitMonitor waiter : Server.waitersPair) {
					if(waiter.getConnectionCounter() == 1) {
						waiterPair = waiter;
						waiterPair.addConnection();
						skip = true;
						break;
					}
				}
				if(!skip) {
					for (WaitMonitor waiter : Server.waitersPair) {
						if(waiter.getConnectionCounter() == 0) {
							waiterPair = waiter;
							waiterPair.addConnection();
							break;
						}
					}
				}
			}
			pairRandom();
			return;
		}
		//"GENERATING CODE AND CREATING GAME ROOM" OPTION
		case "G": {
			synchronized(Server.codesList) {
				code = Server.generateCode();
				Server.codesList.add(code);
			}
			clientOutput.println(code);	//sending generated code to the client
			waiterPair = new WaitMonitor(1);
			while(!isPaired) {
				ExitThread exit = new ExitThread(waiterPair, this, clientInput);
				exit.start();	//the thread that will notify if client has quit
				synchronized(waiterPair) {
					waiterPair.wait();	//waiting for a notification that the opponents are paired or the client has quit
				}
				if(isQuit) return;
				clientOutput.println(pair.username);	//sending pair's username to the client
				if(exit.isAlive()) exit.join();
			}
			return;
		}
		//"ENTERING THE GAME ROOM BY USING THE CODE" OPTION
		case "P": {
			while(true) {
				String input = clientInput.readLine();	//gets the code that the client has entered
				synchronized(Server.codesList) {
					if(!Server.codesList.contains(input)) {
						clientOutput.println("Uneli ste nepostojeci kod.");
						continue;
					} else {
						code = input;
						Server.codesList.remove(code);	//when opponents are paired the code is not in use anymore
						break;
					}
				}
			}
			pairByCode();
			return;
		}
		default:
			setIsQuit(true);
			askToPlayAgain = false;
			return;
		}
	}
	private void startSlagalica() throws IOException, InterruptedException {
		clientOutput.println(rec.getRec());	//sending random generated letters to the client
		String message = clientInput.readLine();
		synchronized(this) {
			if(isQuit) {
				clientOutput.println("Protivnik je napustio igru."); //notifying the client that the opponent has left the game
				clientInput.readLine();
				return;
			}
		}
		slagalicaFinishedWord = message;
		isSlagalicaPlayed = true;
		ExitThread exit = new ExitThread(waiterPair, this, clientInput);
		exit.start();	//the thread that will notify if client has quit
		if(pair.isSlagalicaPlayed) {
			synchronized(waiterPair) {
				pair.slagalicaFinishedWordOfPair = slagalicaFinishedWord;
				pair.isSlagalicaPlayedOfPair = isSlagalicaPlayed;
				slagalicaFinishedWordOfPair = pair.slagalicaFinishedWord;
				isSlagalicaPlayedOfPair = pair.isSlagalicaPlayed;
				waiterPair.notify();	//notifying the opponent that this player has also finished the game
			}
		} else {
			while(!isSlagalicaPlayedOfPair) {
				synchronized(waiterPair) {
					waiterPair.wait();	//waiting for a notification that the opponent has also finished the game or the client has quit
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
		//sending to the client the message about the outcome
		if(slagalicaFinishedWord.length() > slagalicaFinishedWordOfPair.length()) {
			clientOutput.println(slagalicaFinishedWord.length());
		} else if(slagalicaFinishedWord.length() < slagalicaFinishedWordOfPair.length()) {
			clientOutput.println((slagalicaFinishedWordOfPair.length() * -1));
		} else if(slagalicaFinishedWord.length() == 0 && slagalicaFinishedWordOfPair.length() == 0) {
			clientOutput.println("Oba igraca bez bodova!");
		} else {
			clientOutput.println((slagalicaFinishedWordOfPair.length() + 100));
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
		writeMyNumbersJson();	//sending random generated numbers to the client
		String message = clientInput.readLine();
		synchronized(this) {
			if(isQuit) {
				clientOutput.println("Protivnik je napustio igru."); //notifying the client that the opponent has left the game
				clientInput.readLine();
				return;
			}
		}
		mojBrojFinishedNumber = Integer.parseInt(message);
		isMojBrojPlayed = true;
		ExitThread exit = new ExitThread(waiterPair, this, clientInput);
		exit.start();	//the thread that will notify if client has quit
		if(pair.isMojBrojPlayed) {
			synchronized(waiterPair) {
				pair.mojBrojFinishedNumberOfPair = mojBrojFinishedNumber;
				pair.isMojBrojPlayedOfPair = isMojBrojPlayed;
				mojBrojFinishedNumberOfPair = pair.mojBrojFinishedNumber;
				isMojBrojPlayedOfPair = pair.isMojBrojPlayed;
				waiterPair.notify();	//notifying the opponent that this player has also finished the game
			}
		} else {
			while(!isMojBrojPlayedOfPair) {
				synchronized(waiterPair) {
					waiterPair.wait();	//waiting for a notification that the opponent has also finished the game or the client has quit
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
		//sending to the client the message about the outcome
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
	private JSONArray initializeAnswersJSON(Questions questions) {
		JSONArray answers = new JSONArray();
		for (int i = 0; i < 4; i++) {
			answers.add(questions.getAnswer(i));
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
		writePitanja(questionsArray);	//sending random generated questions to the client
		String input;
		int i = 0;
		do {
			input = clientInput.readLine();
			synchronized(this) {
				if(isQuit) {
					clientOutput.println("Protivnik je napustio igru."); //notifying the client that the opponent has left the game
					clientInput.readLine();
					return;
				}
			}
			isCorrectAnswer = input;
			isQuestionAnswered = true;
			ExitThread exit = new ExitThread(waiterPair, this, clientInput);
			exit.start();	//the thread that will notify if client has quit
			if(pair.isQuestionAnswered) {
				synchronized(waiterPair) {
					pair.isCorrectAnswerOfPair = this.isCorrectAnswer;
					pair.isQuestionAnsweredOfPair = true;
					this.isCorrectAnswerOfPair = pair.isCorrectAnswer;
					waiterPair.notify();	//notifying the opponent that this player has also finished the game
				}
			} else {
				while(!isQuestionAnsweredOfPair) {
					synchronized(waiterPair) {
						waiterPair.wait();	//waiting for a notification that the opponent has also finished the game or the client has quit
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
			clientOutput.println(key);	//sending to the client the message about the outcome
			if(exit.isAlive()) exit.join();
			isQuestionAnswered = false; isCorrectAnswer = null; isQuestionAnsweredOfPair = false; isCorrectAnswerOfPair = null;
			i++;
		} while(i < 5);	//repeating five times because the game is consist of five different questions
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
					setIsQuit(true); askToPlayAgain = false;
					e.printStackTrace();
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