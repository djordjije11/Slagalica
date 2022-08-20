package mojbrojClasses;

import java.util.Random;

public class MyNumbers {
	private long[] brojevi = new long[4];
    private long[] srednjiNiz = {10, 15, 20};    //POSTAVLJANJE MOGUCIH OPCIJA ZA RANDOM VREDNOST ZA TO DUGME
    private long[] veciNiz = {25, 50, 100};      //POSTAVLJANJE MOGUCIH OPCIJA ZA RANDOM VREDNOST ZA TO DUGME
    private long srednjiBroj, veciBroj;
    private long wantedNumber;
    private Random random;
    
    public long[] getSrednjiNiz() {
    	return srednjiNiz;
    }
    public long[] getVeciNiz() {
    	return veciNiz;
    }
    public long getWantedNumber() {
    	return wantedNumber;
    }
    public long getBrojeviLength() {
    	return brojevi.length;
    }
    public long getBroj(int i) {
    	return brojevi[i];
    }
    public long getSrednjiBroj() {
    	return srednjiBroj;
    }
    public long getVeciBroj() {
    	return veciBroj;
    }
    
    public MyNumbers(long[] brojevi, long srednjiBroj, long veciBroj, long wantedNumber) {
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
