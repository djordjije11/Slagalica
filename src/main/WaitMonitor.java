package main;

public class WaitMonitor {
	//class created for synchronization between threads when using wait() and notify() methods
	
	public boolean paired = false;	//are ClientHandler instances paired by using this instance
	private int connectionCounter = 0;	//how many ClientHandler instances use this instance
	
	public void removeConnection() {
		connectionCounter--;
	}
	public void addConnection() {
		connectionCounter++;
	}
	public int getConnectionCounter() {
		return connectionCounter;
	}
	public WaitMonitor() {
	}
	public WaitMonitor(int connectionCounter) {
		this.connectionCounter = connectionCounter;
	}
}
