package guiClasses;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import main.WaitMonitor;

public class GameOver extends JFrame implements ActionListener {
	private JButton playAgainButton;
	private WaitMonitor waiter;
	
	public GameOver(WaitMonitor waiter, String username, String usernameOfPair, int score, int scoreOfPair, boolean isGameValid) {
		this.waiter = waiter;
		ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
        this.setIconImage(icon.getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(420, 400);
        this.setResizable(false);
        this.setLayout(null);
        this.setTitle("Kraj!");
        if(!isGameValid) {
        	JLabel label = new JLabel("Igra nije odigrana do kraja.");
        	label.setBounds(100, 30, 200, 40);
        	label.setFont(new Font("Consolas", Font.BOLD, 12));
        	label.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        	this.add(label);
        }
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setBounds(100, 100, 100, 40);
        usernameLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        usernameLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        JLabel usernameOfPairLabel = new JLabel(usernameOfPair);
        usernameOfPairLabel.setBounds(220, 100, 100, 40);
        usernameOfPairLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        usernameOfPairLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        JLabel scoreLabel = new JLabel(Integer.toString(score));
        scoreLabel.setBounds(100, 150, 100, 60);
        scoreLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        scoreLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        JLabel scoreOfPairLabel = new JLabel(Integer.toString(scoreOfPair));
        scoreOfPairLabel.setBounds(220, 150, 100, 60);
        scoreOfPairLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        scoreOfPairLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
        this.add(usernameLabel);
        this.add(usernameOfPairLabel);
        this.add(scoreLabel);
        this.add(scoreOfPairLabel);
        playAgainButton = new JButton("Igraj opet!");
        playAgainButton.setBounds(150, 250, 120, 80);
        playAgainButton.setFocusable(false);
        playAgainButton.addActionListener(this);
		this.add(playAgainButton);
        this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == playAgainButton) {
			synchronized(waiter) {
				waiter.notify();
			}
			this.dispose();
		}
	}
}