package slagalicaClasses;

import java.util.Random;

import main.Server;

public class Rec {
	private String word;
	
	public String getRec() {
		return word;
	}
	public Rec() {
		word = "";
		int size = Server.allCharacters.length;
		Random random = new Random();
		for (int i = 0; i < 12; i++) {
			word = word + ((char) Server.allCharacters[random.nextInt(size)]);
		}
	}
}