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

public class Client {
	private static Socket socketCommunication;
	private static BufferedReader serverInput;
	private static PrintStream serverOutput;
	//private static BufferedReader console;
	
	private static Username usernameGUI;
	private static MyNumbers myNumbers;
	private static MojBroj mojbrojGUI;
	static WaitMonitor waiter;
	
	private static String username;
	
	private static String createFile(String input, String name) {
		FileWriter fw;
		File file = new File(name);
		try {
			fw = new FileWriter(file);
			fw.write(input);
			fw.flush();
			fw.close();
		} catch(IOException ex) {
			Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		return file.getAbsolutePath();
	}
	private static MyNumbers readMyNumbersJson(String path) {
		JSONParser parser = new JSONParser();
		JSONObject object = null;
		try {
			FileReader fr = new FileReader(path);
			object = (JSONObject) parser.parse(fr);
			fr.close();
		} catch (FileNotFoundException ex) {
			 Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		 } catch (IOException ex) {
		 Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		 } catch (ParseException ex) {
		 Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
		 }
		return getMyNumbersFromJSON(object);
	}	
	private static MyNumbers getMyNumbersFromJSON(JSONObject object) {
		JSONArray array = (JSONArray) object.get("brojevi");
		long[] brojevi = new long[4];
		for (int i = 0; i < 4; i++) {
			brojevi[i] = (long) array.get(i);
		}
		long srednjiBroj = (long) object.get("srednjiBroj");
		long veciBroj = (long) object.get("veciBroj");
		long wantedNumber = (long) object.get("wantedNumber");
		return new MyNumbers(brojevi, srednjiBroj, veciBroj, wantedNumber);
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
		input = serverInput.readLine();
		usernameGUI.setPair(input);
	}
	private static void startMojBroj() throws IOException {
		String input;
		input = serverInput.readLine();
		String absloutePath = createFile(input, username + "_myNumbersJSON.json");
		myNumbers = readMyNumbersJson(absloutePath);
		mojbrojGUI = new MojBroj(myNumbers, waiter);
		//new Thread(mojbrojGUI).start();
		synchronized(waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		serverOutput.println(Long.toString(mojbrojGUI.getFinishedNumber()));
		do {
			input = serverInput.readLine();
		} while (input == null);
		mojbrojGUI.setMessage(input);
	}
	
	public static void main(String[] args) {
		try {
			socketCommunication = new Socket("localhost", 9001);
			serverInput = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
			serverOutput = new PrintStream(socketCommunication.getOutputStream());
			//console = new BufferedReader(new InputStreamReader(System.in));
			
			waiter = new WaitMonitor();
			
			setUsername();
			startMojBroj();
			socketCommunication.close();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
