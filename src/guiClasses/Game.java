package guiClasses;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import main.WaitMonitor;

public abstract class Game extends JFrame {
	protected String username;
	protected String usernameOfPair;
	protected JLabel scoreLabel;
	protected int score = 0;
	protected JLabel pairScoreLabel;
	protected int pairScore = 0;
	protected WaitMonitor waiter;
	protected PrintStream serverOutput;
	protected JLabel timeLabel;
	protected boolean isOver = false;
	protected JLabel messageLabel;
	protected TimerTask task;
	protected Timer timer;
	
	public abstract void setMessageLabel(String text);
	public abstract String getResult();
	public int getScores() {
		return score;
	}
	public int getScoresOfPair() {
		return pairScore;
	}
	protected void initializeScoreLabels() {
		scoreLabel = new JLabel();
        scoreLabel.setBounds(120, 40, 200, 20);
        pairScoreLabel = new JLabel();
        pairScoreLabel.setBounds(220, 40, 200, 20);
        updateScoreLabels();
        this.add(scoreLabel);
        this.add(pairScoreLabel);
	}
	private void updateScoreLabels() {
		scoreLabel.setText(username + ": " + score);
		pairScoreLabel.setText(usernameOfPair + ": " + pairScore);
	}
	protected void addScores(int number, int numberForPair) {
		score += number;
		pairScore += numberForPair;
		updateScoreLabels();
	}
	public Game(WaitMonitor waiter, String username, String usernameOfPair, int score, int pairScore, PrintStream serverOutput) {
		this.waiter = waiter;
		this.username = username;
		this.usernameOfPair = usernameOfPair;
		this.score = score;
		this.pairScore = pairScore;
		this.serverOutput = serverOutput;
		ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
        this.setIconImage(icon.getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(420, 400);
        this.setResizable(false);
        this.setLayout(null);
        initializeScoreLabels();
	}
}