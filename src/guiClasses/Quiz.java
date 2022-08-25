package guiClasses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;

import main.WaitMonitor;
import quizClasses.Questions;

public class Quiz extends Gui implements ActionListener {

	private JButton[] answersButton = new JButton[4];
	private JLabel questionLabel;
	private JButton nextButton;
	private Questions[] pitanjaNiz = new Questions[4];
	private String isCorrect;
	private boolean isOver = false;
	private JLabel vreme;
	private int questionCounter = 0;
	private JLabel messageLabel;
	TimerTask task;
	
	public void setMessage(String text) {
		switch (text) {
		case "yes-yes": {
			addScores(5, 5);
			break;
		}
		case "yes-no": {
			addScores(10, -5);
			break;
		}
		case "yes-nothing": {
			addScores(10, 0);
			break;
		}
		case "no-yes": {
			addScores(-5, 10);
			break;
		}
		case "no-no": {
			addScores(-5, -5);
			break;
		}
		case "no-nothing": {
			addScores(-5, 0);
			break;
		}
		case "nothing-yes": {
			addScores(0, 10);
			break;
		}
		case "nothing-no": {
			addScores(0, -5);
			break;
		}
		case "nothing-nothing": {
			addScores(0, 0);
			break;
		}
		default:
			messageLabel.setText(text);
    		return;
		}
	}
	public String getIsCorrect() {
		return isCorrect;
	}
	private void startTime() {
		Timer timer = new Timer();
        task = new TimerTask(){
            int m = 10;
            @Override
            public void run(){
                if(isOver){
                    return;
                }
                if(m > 0) {
                    m--;
                    vreme.setText(Integer.toString(m));
                }else{
                    isCorrect = "nothing";
                    deadButtons();
                    synchronized(waiter) {
                    	waiter.notify();
                    }
                    return;
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
	}
	private void initializeNextButton() {
		nextButton = new JButton("NEXT");
        nextButton.setBounds(150,230,100,30);
        nextButton.setFocusable(false);
        nextButton.addActionListener(this);
        this.add(nextButton);
	}
	private void initializePitanje() {
		int x, y;
        for (int i = 0; i < 4; i++) {
			answersButton[i] = new JButton();
			if(i % 2 == 0) x = 0;
			else x = 185;
			if(i < 2) y = 0;
			else y = 45;
			answersButton[i].setBounds(20 + x, 140 + y, 180, 40);
			answersButton[i].setFocusable(false);
			answersButton[i].addActionListener(this);
			this.add(answersButton[i]);
		}
        questionLabel = new JLabel();
        questionLabel.setBounds(60, 120, 300, 20);
        questionLabel.setFocusable(false);
        this.add(questionLabel);
	}
	private void setPitanje() {
		for (int i = 0; i < 4; i++) {
			answersButton[i].setText(pitanjaNiz[questionCounter].getAnswer(i));
			answersButton[i].setEnabled(true);
		}
		nextButton.setEnabled(true);
		questionLabel.setText(pitanjaNiz[questionCounter].getQuestion());
		startTime();
	}
	public void addScores(int number, int numberForPair) {
		super.addScores(number, numberForPair);
		questionCounter++;
		if(questionCounter < pitanjaNiz.length) {
			isOver = false;
			setPitanje();
		} else {
			isOver = true;
			//dispose();
		}
	}
	public Quiz(Questions[] pitanjaNiz, WaitMonitor waiter, String username, String usernameOfPair, int score, int pairScore, PrintStream serverOutput) {
		super(waiter, username, usernameOfPair, score, pairScore, serverOutput);
		this.pitanjaNiz = pitanjaNiz;
        this.setTitle("Quiz");
        initializePitanje();
        initializeNextButton();
        
    	vreme = new JLabel("10");
        vreme.setBounds(10, 20, 40, 40);
        this.add(vreme);
        
        messageLabel = new JLabel();
        messageLabel.setBounds(100, 300, 300, 50);
        this.add(messageLabel);
    	
        this.setVisible(true);
        setPitanje();
	}
	private void deadButtons() {
		for (int i = 0; i < answersButton.length; i++) {
			answersButton[i].setEnabled(false);
		}
		nextButton.setEnabled(false);
		isOver = true;
		task.cancel();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		for (int i = 0; i < answersButton.length; i++) {
			if(e.getSource() == answersButton[i]) {
				if(answersButton[i].getText().equals(pitanjaNiz[questionCounter].getCorrectAnswer())) {
					isCorrect = "yes";
				} else {
					isCorrect = "no";
				}
				deadButtons();
				synchronized(waiter) {
					waiter.notify();
				}
				return;
			}
		}
		if(e.getSource() == nextButton) {
			isCorrect = "nothing";
			deadButtons();
			synchronized(waiter) {
				waiter.notify();
			}
			return;
		}
	}
}
