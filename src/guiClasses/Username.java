package guiClasses;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import main.WaitMonitor;

public class Username extends JFrame implements ActionListener {

	private JButton sendButton;
	private JTextField textField;
	private JLabel label1;
	private JLabel label2;
	private String username;
	private WaitMonitor waiter;
	
	public void setMessage(String text) {
		if(text.equals("Uneti username je vec koriscen. Pokusaj opet!")) {
			label2.setText(text);
			username = null;
			textField.setText("");
			textField.setEnabled(true);
			sendButton.setEnabled(true);
		} else {
			label2.setText(text);
			this.dispose();
		}
	}
	
	public String getUsername() {
		return username;
	}
	
	public Username(WaitMonitor waiter){
		this.waiter = waiter;
		ImageIcon icon = new ImageIcon("images\\slagalica.jpg");
		this.setIconImage(icon.getImage());
        this.setTitle("Slagalica");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(420, 400);
        this.setResizable(false);
        this.setLayout(null);
        
        label1 = new JLabel("Unesite vas username:");
        label1.setBounds(140, 100, 170, 40);

        label2 = new JLabel();
        label2.setBounds(70, 250, 280, 40);
        
        textField = new JTextField();
        textField.setBounds(110, 140, 200, 40);
        textField.setFont(new Font("Consolas", Font.BOLD, 26));
        
        sendButton = new JButton("SEND");
        sendButton.setBounds(170, 188, 80, 40);
        sendButton.setFocusable(false);
        sendButton.addActionListener(this);
        
        this.add(label1);
        this.add(label2);
        this.add(textField);
        this.add(sendButton);
        
        this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == sendButton) {
			String text = textField.getText();
			if(text == null || text.contains(" ") || !text.matches(".*[a-zA-Z]+.*")) {
				textField.setText("");
				label2.setText("Uneti username je neadekvatan. Pokusaj opet!");
			} else {
				textField.setEnabled(false);
				sendButton.setEnabled(false);
				username = text;
				synchronized(waiter) {
					waiter.notify();
				}
			}
		}
	}
}
