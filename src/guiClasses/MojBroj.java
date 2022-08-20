package guiClasses;

import javax.swing.*;

import main.WaitMonitor;
import mojbrojClasses.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class MojBroj extends JFrame implements ActionListener/*, Runnable*/ {
    private JButton buttonSrednji;
    private JButton buttonVeci;
    private JButton[] buttonBrojevi;
    private JButton buttonOtvorenaZagrada;
    private JButton buttonZatvorenaZagrada;
    private JButton buttonPlus;
    private JButton buttonMinus;
    private JButton buttonPuta;
    private JButton buttonDeljenje;
    private JButton buttonReset;
    private JButton buttonDelete;
    private JButton buttonFinish;
    private JLabel labelBroj;       //LABELA ZA BROJ KOJI TREBA DA DOBIJEMO, RANDOM IZGENERISAN
    private JLabel labelResult;		//LABELA ZA BROJ KOJI SMO DOBILI RACUNANJEM, I ZA SAM ISPIS RACUNA
    private JLabel message;
    private long result;         //BROJ KOJI SMO DOBILI RACUNANJEM
    private WaitMonitor waiter;
    private MyNumbers myNumbers;
    
    private long finishedNumber;	//RAZLIKA IZMEDJU TRAZENOG BROJA I DOBIJENOG, SLUZI DA BI SE UPOREDIO KASNIJE REZULTAT IZMEDJU PROTIVNIKA

    private JLabel vreme;
    private boolean isOver = false;

    static int countStringInString(String wholeString, String countPart){
        if(wholeString == null || countPart == null){
            System.out.println("GRESKA PRILIKOM KORISCENJA countStringInString METODE.");
            return -1;
        }
        return wholeString.length() - wholeString.replace(countPart, "").length();
    }
    static boolean arrayContains(long[] array, long number){
        for (int i = 0; i < array.length; i++){
            if(array[i] == number) return true;
        }
        return false;
    }
    
    public long getFinishedNumber() {
    	return finishedNumber;
    }

    public void setMessage(String text) {
    	message.setText(text);
    	//Thread.sleep(1500);
    	//this.dispose();
    	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public MojBroj(MyNumbers myNumbers, WaitMonitor waiter){
    	this.myNumbers = myNumbers;
    	this.waiter = waiter;
        int i;
        ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
        this.setIconImage(icon.getImage());
        this.setTitle("Moj Broj");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setSize(420, 400);
        this.setResizable(false);
        this.setLayout(null);
        //INICIJALIZOVANJE PRVA 4 DUGMETA ZA BROJEVE
        int k = 0;
        buttonBrojevi = new JButton[4];
        for(i = 0; i < buttonBrojevi.length; i++){
            buttonBrojevi[i] = new JButton(Long.toString(myNumbers.getBroj(i)));
            buttonBrojevi[i].setBounds(10 + k, 70, 50, 50);
            k += 50;
            buttonBrojevi[i].setFocusable(false);
            buttonBrojevi[i].addActionListener(this);
            this.add(buttonBrojevi[i]);
        }
        //INICIJALIZOVANJE PREOSTALA 2 DUGMETA ZA BROJEVE
        buttonSrednji = new JButton(Long.toString(myNumbers.getSrednjiBroj()));
        buttonSrednji.setBounds(215, 70, 90, 50);
        buttonSrednji.setFocusable(false);
        buttonSrednji.addActionListener(this);
        this.add(buttonSrednji);
        buttonVeci = new JButton(Long.toString(myNumbers.getVeciBroj()));
        buttonVeci.setBounds(305, 70, 90, 50);
        buttonVeci.setFocusable(false);
        buttonVeci.addActionListener(this);
        this.add(buttonVeci);
        //INICIJALIZOVANJE DUGMICA ZA OPERATORE I ZAGRADE
        buttonPlus = new JButton("+");
        buttonPlus.setBounds(15, 145, 60, 25);
        buttonPlus.setFocusable(false);
        buttonPlus.addActionListener(this);
        this.add(buttonPlus);
        buttonMinus = new JButton("-");
        buttonMinus.setBounds(75, 145, 60, 25);
        buttonMinus.setFocusable(false);
        buttonMinus.addActionListener(this);
        this.add(buttonMinus);
        buttonPuta = new JButton("*");
        buttonPuta.setBounds(15, 170, 60, 25);
        buttonPuta.setFocusable(false);
        buttonPuta.addActionListener(this);
        this.add(buttonPuta);
        buttonDeljenje = new JButton("/");
        buttonDeljenje.setBounds(75, 170, 60, 25);
        buttonDeljenje.setFocusable(false);
        buttonDeljenje.addActionListener(this);
        this.add(buttonDeljenje);
        buttonOtvorenaZagrada = new JButton("(");
        buttonOtvorenaZagrada.setBounds(135, 145, 70,25);
        buttonOtvorenaZagrada.setFocusable(false);
        buttonOtvorenaZagrada.addActionListener(this);
        this.add(buttonOtvorenaZagrada);
        buttonZatvorenaZagrada = new JButton(")");
        buttonZatvorenaZagrada.setBounds(135, 170, 70,25);
        buttonZatvorenaZagrada.setFocusable(false);
        buttonZatvorenaZagrada.addActionListener(this);
        this.add(buttonZatvorenaZagrada);
        //INICIJALIZOVANJE DUGMETA ZA RESETOVANJE DOSAD URADJENOG RACUNA
        buttonReset = new JButton("RESET");
        buttonReset.setBounds(305, 145, 85, 25);
        buttonReset.setFocusable(false);
        buttonReset.addActionListener(this);
        this.add(buttonReset);
        //INICIJALIZOVANJE DUGMETA ZA BRISANJE UKUCANOG
        buttonDelete = new JButton("DELETE");
        buttonDelete.setBounds(220, 145, 85, 25);
        buttonDelete.setFocusable(false);
        buttonDelete.addActionListener(this);
        this.add(buttonDelete);
        //INICIJALIZOVANJE DUGMETA ZA PROVERU RACUNA
        buttonFinish = new JButton("FINISH");
        buttonFinish.setBounds(220, 170, 170, 25);
        buttonFinish.setFocusable(false);
        buttonFinish.addActionListener(this);
        this.add(buttonFinish);
        //INICIJALIZOVANJE LABELE ZA ISPIS RACUNA
        labelResult = new JLabel("RACUN: ");
        labelResult.setBounds(10, 200, 400, 50);
        this.add(labelResult);
        //INICIJALIZOVANJE LABELE ZA TRAZENI BROJ
        labelBroj = new JLabel("Trazeni broj: " + myNumbers.getWantedNumber());
        labelBroj.setBounds(136, 20, 100, 40);
        this.add(labelBroj);

        //VREME
        vreme = new JLabel("60");
        vreme.setBounds(10, 20, 40, 40);
        this.add(vreme);
        
        message = new JLabel();
        message.setBounds(100, 300, 300, 50);
        this.add(message);
        
        /*
        try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		this.setVisible(true);

        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            int m = 60;
            @Override
            public void run(){
                if(isOver){
                    return;
                }
                if(m > 0) {
                    m--;
                    vreme.setText(Integer.toString(m));
                }else{
                    end();
                }
            }
        };
        //timer.schedule(task, 10000);
        timer.scheduleAtFixedRate(task, 0, 1000);

    }

    private void endButtons() {
        for (int i = 0; i < buttonBrojevi.length; i++){
            buttonBrojevi[i].setEnabled(false);
        }
        buttonSrednji.setEnabled(false);
        buttonVeci.setEnabled(false);
        buttonPlus.setEnabled(false);
        buttonMinus.setEnabled(false);
        buttonPuta.setEnabled(false);
        buttonDeljenje.setEnabled(false);
        buttonDelete.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonFinish.setEnabled(false);
        buttonOtvorenaZagrada.setEnabled(false);
        buttonZatvorenaZagrada.setEnabled(false);
        isOver = true;
        message.setText("Ceka se protivnik...");
        synchronized(waiter) {
        	waiter.notify();
        }
    }
    
    private void end(){
    	String text = labelResult.getText();
        text = text.substring(new String("RACUN: ").length());
        try{
            double d = Evaluation.eval(text);
            int a = (int)d;
            if(a==d){
                result = a;
                if(result == myNumbers.getWantedNumber()){
                	finishedNumber = 0;
                    labelResult.setText("Dobijen je tacan broj: " + result);
                    endButtons();
                    return;
                } else{
                	finishedNumber = Math.abs(result - myNumbers.getWantedNumber());
                    labelResult.setText("Dobijeni broj je udaljen od trazenog za " + finishedNumber);
                    endButtons();
                    return;
                }
            } else{
            	finishedNumber = Integer.MAX_VALUE;
                labelResult.setText("GRESKA, PRI DELJENJU NIJE DOBIJEN CEO BROJ");
                endButtons();
                return;
            }
        } catch(Exception z){
            labelResult.setText("GRESKA PRI FUNKCIJI EVAL");
            endButtons();
            return;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i = 0; i < buttonBrojevi.length; i++){
            if(e.getSource() == buttonBrojevi[i]){
                String text = labelResult.getText();
                char c = text.charAt(text.length()-1);
                if(c == '+' || c == '-' || c == '*' || c == '/' || c == ' ' || c == '('){
                    labelResult.setText(text + Long.toString(myNumbers.getBroj(i)));
                    buttonBrojevi[i].setEnabled(false);
                }
                return;
            }
        }
        if(e.getSource() == buttonSrednji){
            String text = labelResult.getText();
            char c = text.charAt(text.length()-1);
            if(c == '+' || c == '-' || c == '*' || c == '/' || c == ' ' || c == '('){
                labelResult.setText(text + Long.toString(myNumbers.getSrednjiBroj()));
                buttonSrednji.setEnabled(false);
            }
            return;
        }
        if(e.getSource() == buttonVeci){
            String text = labelResult.getText();
            char c = text.charAt(text.length()-1);
            if(c == '+' || c == '-' || c == '*' || c == '/' || c == ' ' || c == '('){
                labelResult.setText(text + Long.toString(myNumbers.getVeciBroj()));
                buttonVeci.setEnabled(false);
            }
            return;
        }
        if(e.getSource() == buttonPlus){
            String text = labelResult.getText();
            char c = text.charAt(text.length() - 1);
            if(c != ' ' && c != '+' && c != '-' && c != '*' && c != '/' && c != '(') {
                labelResult.setText(text + "+");
            }
            return;
        }
        if(e.getSource() == buttonMinus){
            String text = labelResult.getText();
            char c = text.charAt(text.length() - 1);
            if(c != ' ' && c != '+' && c != '-' && c != '*' && c != '/' && c != '(') {
                labelResult.setText(text + "-");
            }
            return;
        }
        if(e.getSource() == buttonPuta){
            String text = labelResult.getText();
            char c = text.charAt(text.length() - 1);
            if(c != ' ' && c != '+' && c != '-' && c != '*' && c != '/' && c != '(') {
                labelResult.setText(text + "*");
            }
            return;
        }
        if(e.getSource() == buttonDeljenje){
            String text = labelResult.getText();
            char c = text.charAt(text.length() - 1);
            if(c != ' ' && c != '+' && c != '-' && c != '*' && c != '/' && c != '(') {
                labelResult.setText(text + "/");
            }
            return;
        }
        if(e.getSource() == buttonOtvorenaZagrada){
            String text = labelResult.getText();
            char c = text.charAt(text.length()-1);
            if(c == '+' || c == '-' || c == '*' || c == '/' || c == ' ' || c == '('){
                labelResult.setText(text + "(");
            }
            return;
        }
        if(e.getSource() == buttonZatvorenaZagrada){
            String text = labelResult.getText();
            char c = text.charAt(text.length()-1);
            if(c != '+' && c != '-' && c != '*' && c != '/' && c != ' ' && c != '('
                    && countStringInString(text,"(") > countStringInString(text, ")")){
                labelResult.setText(text + ")");
            }
            return;
        }
        if(e.getSource() == buttonReset){
            labelResult.setText("RACUN: ");
            for(int i = 0; i < buttonBrojevi.length; i++){
                buttonBrojevi[i].setEnabled(true);
            }
            buttonSrednji.setEnabled(true);
            buttonVeci.setEnabled(true);
            isOver = false;
            return;
        }
        if(e.getSource() == buttonDelete){
            String text = labelResult.getText();
            char c1 = text.charAt(text.length() - 1);
            if(c1 == ' ') return;
            if(c1 == '+' || c1 == '-' || c1 == '*' || c1 == '/' || c1 == '(' || c1 == ')'){
                text = text.substring(0, text.length()-1);
                labelResult.setText(text);
                return;
            }
            char c2 = text.charAt(text.length() - 2);
            if(c2 == '+' || c2 == '-' || c2 == '*' || c2 == '/' || c2 == '(' || c2 == ')' || c2 == ' '){
                try{
                    int a = Integer.parseInt(Character.toString(c1));
                    for (int i = 0; i < myNumbers.getBrojeviLength(); i++){
                        if(a == myNumbers.getBroj(i) && buttonBrojevi[i].isEnabled() == false){
                            text = text.substring(0, text.length() - 1);
                            labelResult.setText(text);
                            buttonBrojevi[i].setEnabled(true);
                            return;
                        }
                    }
                } catch(NumberFormatException z){
                    System.out.println("GRESKA PRILIKOM PARSIRANJU STRINGA U INT - buttonDELETE");
                    return;
                }
            }
            char c3 = text.charAt(text.length() - 3);
            if(c3 == '1'){
                String number = "" + c3 + c2 + c1;
                try{
                    Integer.parseInt(number);
                    text = text.substring(0, text.length() - 3);
                    labelResult.setText(text);
                    buttonVeci.setEnabled(true);
                    return;
                } catch(NumberFormatException z){
                    System.out.println("GRESKA PRILIKOM PARSIRANJU STRINGA U INT - buttonDELETE");
                    return;
                }
            } else{
                String number = "" + c2 + c1;
                try{
                    int a = Integer.parseInt(number);
                    text = text.substring(0, text.length() - 2);
                    if(arrayContains(myNumbers.getSrednjiNiz(), a)){
                        labelResult.setText(text);
                        buttonSrednji.setEnabled(true);
                        return;
                    }
                    if(arrayContains(myNumbers.getVeciNiz(), a)){
                        labelResult.setText(text);
                        buttonVeci.setEnabled(true);
                        return;
                    }
                } catch(NumberFormatException z){
                    System.out.println("GRESKA PRILIKOM PARSIRANJU STRINGA U INT - buttonDELETE");
                    return;
                }
            }
        }
        //PODSETNIK - OBEZBEDITI DA NA KRAJU IGRE IGRAC NE MOZE DA KORISTI OPCIJE DELETE, kucanja znakova itd.
        if(e.getSource() == buttonFinish){
            end();
        }
    }
    
    /*
	@Override
	public void run() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setVisible(true);

        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            int m = 60;
            @Override
            public void run(){
                if(isOver){
                    return;
                }
                if(m > 0) {
                    m--;
                    vreme.setText(Integer.toString(m));
                }else{
                    end();
                }
            }
        };
        //timer.schedule(task, 10000);
        timer.scheduleAtFixedRate(task, 0, 1000);
	}
	*/
}