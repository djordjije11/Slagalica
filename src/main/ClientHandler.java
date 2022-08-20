package main;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mojbrojClasses.MyNumbers;

public class ClientHandler extends Thread {
	private BufferedReader clientInput = null;
	private PrintStream clientOutput = null;
	private Socket socketCommunication = null;
	
	private String username;
	public ClientHandler pair;
	public boolean isPaired = false;
	private boolean isQuit = false;
	private WaitMonitor waiterPair;
	private WaitMonitor waiterMojBroj;
	
	public MyNumbers myNumbers;
	
	public boolean isMojBrojPlayed = false;
	public boolean isMojBrojPlayedOfPair = false;
	public int mojBrojFinishedNumber;
	public int mojBrojFinishedNumberOfPair;
	
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
	ClientHandler(Socket socketCommunication, WaitMonitor waiterPair, WaitMonitor waiterMojBroj){
		this.socketCommunication = socketCommunication;
		this.waiterPair = waiterPair;
		this.waiterMojBroj = waiterMojBroj;
	}
	ClientHandler(String username){
		this.username = username;
	}
	private void pair() {
		synchronized(waiterPair) {
			for (ClientHandler client : Server.onlineUsers) {
				if(client != this && client.isPaired == false) {
					client.pair = this;
					this.pair = client;
					client.isPaired = true;
					this.isPaired = true;
					myNumbers = new MyNumbers();
					client.myNumbers = myNumbers;
					waiterPair.notify();
					break;
				}
			}
		}
		while(!isPaired) {
			synchronized(waiterPair) {
				try {
					waiterPair.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		clientOutput.println(">>> Tvoj par je " + pair.username);
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
	private void startMojBroj() throws IOException {
		writeMyNumbersJson(username + "_myNumbersJSON.json");
		String message;
		do {
			message = clientInput.readLine();
		} while (message == null);
		//Primljen je mojBrojFinishedNumber iz igre Moj Broj
		mojBrojFinishedNumber = Integer.parseInt(message);
		isMojBrojPlayed = true;
		if(pair.isMojBrojPlayed) {
			synchronized(waiterMojBroj) {
				pair.mojBrojFinishedNumberOfPair = mojBrojFinishedNumber;
				pair.isMojBrojPlayedOfPair = isMojBrojPlayed;
				mojBrojFinishedNumberOfPair = pair.mojBrojFinishedNumber;
				isMojBrojPlayedOfPair = pair.isMojBrojPlayed;
				waiterMojBroj.notify();
			}
		} else {
			while(!isMojBrojPlayedOfPair) {
				synchronized(waiterMojBroj) {
					try {
						waiterMojBroj.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
	
	@Override
	public void run() {
		try {
			clientInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			clientOutput = new PrintStream(socketCommunication.getOutputStream());
			
			setUsername();
			while(!isQuit) {
				if(!isPaired) {
					pair();
				}
				startMojBroj();
				isQuit = true;
				break;
			}
			
			Server.onlineUsers.remove(this);
			socketCommunication.close();
			System.out.println("Konekcija zatvorena.");
		} catch (IOException e) {
			if(username != null) {
				Server.onlineUsers.remove(this);
			}
			System.out.println("Konekcija zatvorena.");
		}
	}
}
