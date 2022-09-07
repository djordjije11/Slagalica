package main;

public class WaitMonitor {
	public boolean paired = false;
	
	private int connectionCounter = 0;
	
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
