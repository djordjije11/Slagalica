package mojbrojClasses;

import java.util.Random;

public class MyNumbers {
	private int[] brojevi = new int[4];
    private int[] srednjiNiz = {10, 15, 20};    //POSTAVLJANJE MOGUCIH OPCIJA ZA RANDOM VREDNOST ZA TO DUGME
    private int[] veciNiz = {25, 50, 100};      //POSTAVLJANJE MOGUCIH OPCIJA ZA RANDOM VREDNOST ZA TO DUGME
    private int srednjiBroj, veciBroj;
    private int wantedNumber;
    private Random random;
    
    public int[] getSrednjiNiz() {
    	return srednjiNiz;
    }
    public int[] getVeciNiz() {
    	return veciNiz;
    }
    public int getWantedNumber() {
    	return wantedNumber;
    }
    public int getBrojeviLength() {
    	return brojevi.length;
    }
    public int getBroj(int i) {
    	return brojevi[i];
    }
    public int getSrednjiBroj() {
    	return srednjiBroj;
    }
    public int getVeciBroj() {
    	return veciBroj;
    }
    
    public MyNumbers(int[] brojevi, int srednjiBroj, int veciBroj, int wantedNumber) {
    	this.brojevi = brojevi;
    	this.srednjiBroj = srednjiBroj;
    	this.veciBroj = veciBroj;
    	this.wantedNumber = wantedNumber;
    }
    public MyNumbers(){
    	random = new Random();
        //DODELJIVANJE BROJEVIMA RANDOM VREDNOST OD 1 DO 9
        for (int i = 0; i < brojevi.length; i++){
            brojevi[i] = random.nextInt(9) + 1;
        }
        srednjiBroj = srednjiNiz[random.nextInt(3)];
        veciBroj = veciNiz[random.nextInt(3)];
        wantedNumber = random.nextInt(999) + 1;
    }
}
