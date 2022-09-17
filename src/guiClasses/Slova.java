package guiClasses;

import javax.swing.*;
import main.WaitMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

public class Slova extends Game implements ActionListener {
	static boolean ukljuciProveruReci = false; //database is not complete, so checking if the word is valid is turned off
    private JButton[] buttonSlova;
    private JButton buttonDelete;
    private JButton buttonFinish;
    private String slova;
    private String finishedRec;
    private JLabel labelResult;	
    private JLabel labelRec; 
    File citajTextFile = new File("baza\\baza_reci.txt");
    
    public String getResult() {
    	return finishedRec;
    }
    public void setMessageLabel(String text) {
    	if(text.equals("Protivnik je napustio igru.")) {
    		messageLabel.setText(text);
    	}else if(text.equals("Oba igraca bez bodova!")) {
    		//players don't get points
    		messageLabel.setText(text);
    	} else {
    		int l = Integer.parseInt(text);
        	int p = l * 2;
        	if (l > 100) {
    			l -= 100;
    			p = l * 2;
    			l = 0;
    		}
        	if(l == 0) {
        		messageLabel.setText("Nereseno!");
        		addScores(p, p);
        	} else if(l < 0) {
        		p *= -1;
        		messageLabel.setText("Izgubili ste!");
        		addScores(0, p);
        	}else {
        		messageLabel.setText("Pobedili ste!");
        		addScores(p, 0);
        	}
    	}
    	try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	this.dispose();
    }
    private void startTime() {
    	timer = new Timer();
        task = new TimerTask(){
            int m = 60;
            @Override
            public void run(){
                if(isOver){
                    return;
                }
                if(m > 0) {
                    m--;
                    timeLabel.setText(Integer.toString(m));
                }else{
                    end();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public Slova(String slova, WaitMonitor waiter, String username, String usernameOfPair, int score, int scoreOfPair, PrintStream serverOutput){
    	super(waiter, username, usernameOfPair, score, scoreOfPair, serverOutput);
    	this.slova = slova;
        this.setTitle("Slagalica");
        this.setSize(640, 400);
        //INICIJALIZOVANJE PRVA 12 DUGMETA ZA BROJEVE
        int k = 0;
        buttonSlova = new JButton[12];
        for(int i = 0; i < buttonSlova.length; i++){
            buttonSlova[i] = new JButton(""+slova.charAt(i));
            buttonSlova[i].setBounds(10 + k, 120, 50, 50);
            k += 50;
            buttonSlova[i].setFocusable(false);
            buttonSlova[i].addActionListener(this);
            this.add(buttonSlova[i]);
        }
        //INICIJALIZOVANJE DUGMETA ZA BRISANJE UKUCANOG
        buttonDelete = new JButton("DELETE");
        buttonDelete.setBounds(10, 195, 100, 50);
        buttonDelete.setFocusable(false);
        buttonDelete.addActionListener(this);
        this.add(buttonDelete);
        //INICIJALIZOVANJE DUGMETA ZA SLANJE RECI
        buttonFinish = new JButton("FINISH");
        buttonFinish.setBounds(110, 195, 100, 50);
        buttonFinish.setFocusable(false);
        buttonFinish.addActionListener(this);
        this.add(buttonFinish);
        labelResult = new JLabel("");
        labelResult.setBounds(10, 250, 400, 50);
        this.add(labelResult);
        labelRec = new JLabel();
        labelRec.setBounds(100, 300, 300, 50);
        this.add(labelRec);
        //timeLabel
        timeLabel = new JLabel("60");
        timeLabel.setBounds(10, 20, 40, 40);
        this.add(timeLabel);
        messageLabel = new JLabel();
        messageLabel.setBounds(100, 300, 300, 50);
        this.add(messageLabel);
		this.setVisible(true);
        startTime();
    }
    private void endButtons() {
        for (int i = 0; i < buttonSlova.length; i++){
            buttonSlova[i].setEnabled(false);
        }
        buttonDelete.setEnabled(false);
        buttonFinish.setEnabled(false);
        isOver = true;
        task.cancel();
        messageLabel.setText("Ceka se protivnik...");
        synchronized(waiter) {
        	waiter.notify();
        }
    }
    private static boolean daLiImaTaRec(File bazaFileWriter, String rec) throws FileNotFoundException, IOException {
		String linijaString = "";
		try(BufferedReader bReader= new BufferedReader(new FileReader(bazaFileWriter))){
	         while ((linijaString = bReader.readLine())!= null) {
					String[] vrednostiStrings = linijaString.split(",");
					for (int i = 0; i < vrednostiStrings.length; i++) {
						if (stringCompare(rec, vrednostiStrings[i]) == 0) {
							return true;
						}
					}
				}
	         return !ukljuciProveruReci;
		}
	}
	public static int stringCompare(String str1, String str2) {
	    int l1 = str1.length();
	    int l2 = str2.length();
	    int lmin = Math.min(l1, l2);
	    for (int i = 0; i < lmin; i++) {
	        int str1_ch = (int)str1.charAt(i);
	        int str2_ch = (int)str2.charAt(i);
	        if (str1_ch != str2_ch) {
	            return 1;
	        }
	    }
	    if (l1 != l2) {
	        return 1;
	    }
	    else {
	        return 0;
	    }
	}
    private void end(){
        try{
        	if(daLiImaTaRec(citajTextFile, labelResult.getText())) {
        		endButtons();
                finishedRec = labelResult.getText();
                return;
			}
        	 labelResult.setText("REC NE POSTOJI");
        	 finishedRec = "";
        	 endButtons();
        	 return;
        } catch(Exception z){
            labelResult.setText("GRESKA, NIJE UNETA REC");
            finishedRec = "";
            endButtons();
            return;
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i = 0; i < buttonSlova.length; i++){
            if(e.getSource() == buttonSlova[i]){
                String text = labelResult.getText()+ buttonSlova[i].getText();
                labelResult.setText(text);
                buttonSlova[i].setEnabled(false);
                return;
            }
        }
        if(e.getSource() == buttonDelete){
            String text = labelResult.getText();
            if(text.equals(" ") || text.equals("")) return;
            char c = text.charAt(text.length() - 1);
            try{
            	for (int i = 0; i < 12; i++){
                    if(c == slova.charAt(i) && buttonSlova[i].isEnabled() == false){
                        text = text.substring(0, text.length() - 1);
                        labelResult.setText(text);
                        buttonSlova[i].setEnabled(true);
                        return;
                    }
                }
            } catch(NumberFormatException z){
            	System.out.println("GRESKA PRILIKOM KLIKA NA DUGME - buttonDELETE");
                return;
            }
        }
        if(e.getSource() == buttonFinish){
            end();
        }
    }
}