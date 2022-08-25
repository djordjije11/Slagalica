package guiClasses;

import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import main.WaitMonitor;

public class Pairing extends JFrame implements ActionListener {
	
	private JButton randomPairingButton;
    private JButton getCodeButton;
	private JButton putCodeButton;
	private JLabel usernameLabel;
	private JLabel pairLabel;
	private TextArea codeArea;
	private JButton sendCodeButton;
	private WaitMonitor waiter;
	private char message;
	private String code;
	//private PrintStream serverOutput;
	
	public String getCode() {
		return code;
	}
	
	public void repeatCode(String text) {
		pairLabel.setText(text);
		enableAreaForCode(true);
	}
	
	public char getMessage() {
		return message;
	}
	
	public void setCode(String code) {
		pairLabel.setText(code);
	}
	
	public void setPairLabel(String pairUsername) {
		pairLabel.setText(">>> Tvoj par je " + pairUsername);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.dispose();
	}
	
	public Pairing(WaitMonitor waiter, String username, PrintStream serverOutput) {
		this.waiter = waiter;
		//this.serverOutput = serverOutput;
		ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
		this.setIconImage(icon.getImage());
        this.setTitle("Slagalica");
        //this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(420, 450);
        this.setResizable(false);
        this.setLayout(null);
        
		usernameLabel = new JLabel("Zdravo " + username + "!");
		usernameLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		usernameLabel.setBounds(40, 10, 340, 20);
		this.add(usernameLabel);
		
		pairLabel = new JLabel();
		pairLabel.setHorizontalAlignment((int) CENTER_ALIGNMENT);
		pairLabel.setBounds(40, 370, 340, 20);
		this.add(pairLabel);
		
		randomPairingButton = new JButton("NASUMICNO POVEZIVANJE");
		randomPairingButton.setBounds(40, 30, 340, 70);
		randomPairingButton.setFocusable(false);
		randomPairingButton.addActionListener(this);
		this.add(randomPairingButton);
		getCodeButton = new JButton("GENERISI KOD ZA OTVARANJE SOBE");
		getCodeButton.setBounds(40, 110, 340, 70);
		getCodeButton.setFocusable(false);
		getCodeButton.addActionListener(this);
		this.add(getCodeButton);
		putCodeButton = new JButton("PRISTUPI SOBI UNOSENJEM KODA");
		putCodeButton.setBounds(40, 190, 340, 70);
		putCodeButton.setFocusable(false);
		putCodeButton.addActionListener(this);
		this.add(putCodeButton);
		
		codeArea = new TextArea();
		codeArea.setBounds(80, 270, 280, 40);
		sendCodeButton = new JButton("SEND");
		sendCodeButton.setBounds(100, 320, 220, 30);
		sendCodeButton.setFocusable(false);
		sendCodeButton.addActionListener(this);
		enableAreaForCode(false);
		codeArea.setVisible(false);
		sendCodeButton.setVisible(false);
		this.add(codeArea);
		this.add(sendCodeButton);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				serverOutput.println("EXIT");
				System.exit(0);
			}
		});
		
		this.setVisible(true);
	}
	
	private void addAreaForCode() {
		enableAreaForCode(true);
		codeArea.setVisible(true);
		sendCodeButton.setVisible(true);
		this.setVisible(true);
	}
	
	private void enableAreaForCode(boolean hm) {
		codeArea.setEnabled(hm);
		sendCodeButton.setEnabled(hm);
	}
	
	private void deadButtons() {
		randomPairingButton.setEnabled(false);
		getCodeButton.setEnabled(false);
		putCodeButton.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == randomPairingButton) {
			message = 'R';
			deadButtons();
			pairLabel.setText("Trazimo para...");
			synchronized(waiter) {
				waiter.notify();
			}
			return;
		}
		if(e.getSource() == getCodeButton) {
			message = 'G';
			deadButtons();
			synchronized(waiter) {
				waiter.notify();
			}
			return;
		}
		if(e.getSource() == putCodeButton) {
			message = 'P';
			deadButtons();
			synchronized(waiter) {
				waiter.notify();
			}
			addAreaForCode();
			return;
		}
		if(e.getSource() == sendCodeButton) {
			if(!codeArea.getText().matches("[1-9][0-9]{7}")) {
				pairLabel.setText("Kod nije unet u pravilnom obliku.");
				return;
			}
			code = codeArea.getText();
			enableAreaForCode(false);
			synchronized(waiter) {
				waiter.notify();
			}
			return;
		}
	}
}
