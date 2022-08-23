package main;

public class WaitMonitor {
	private int connectionCounter = 0;
	
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
