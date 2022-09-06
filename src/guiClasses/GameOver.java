package guiClasses;

import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GameOver extends JFrame {

	public GameOver(String username, String usernameOfPair, int score, int scoreOfPair) {
		ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
        this.setIconImage(icon.getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(420, 400);
        this.setResizable(false);
        this.setLayout(null);
        this.setTitle("Kraj!");
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
        this.setVisible(true);
	}
}
