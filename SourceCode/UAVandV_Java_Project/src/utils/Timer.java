package utils;

import java.util.concurrent.Semaphore;

import javafx.scene.control.TextField;

public class Timer implements Runnable {

	public static final int ONE_SECOND = 999; // 999 Milliseconds
	
	
	public int hours;
	public int minutes;
	public int seconds;
	
	public boolean isPause;
	public boolean terminate;
	
	private TextField tfTimer;
	
	//Semaphores
	private Semaphore terminateSemaphore;
	private Semaphore pauseSemaphore;
	
	public Timer(TextField tfTimer) {
		super();
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
		this.isPause = false;
		this.terminate = false;
		this.tfTimer = tfTimer;
		
		terminateSemaphore = new Semaphore(1);
		pauseSemaphore = new Semaphore(1);
	}

	@Override
	public void run() {
		
		try { terminateSemaphore.acquire();} catch (InterruptedException e) { }
		
		while(!this.terminate) {
			terminateSemaphore.release();
			
			try { pauseSemaphore.acquire();} catch (InterruptedException e) { }
			if(!this.isPause) {
				pauseSemaphore.release();
				// Seconds
				if(this.seconds < 60) 
					this.seconds++;
				else {
					this.minutes++;
					this.seconds = 0;
				}
				// Minutes
				if(this.minutes > 59) {
					this.minutes = 0;
					this.hours++;
				}
				try { Thread.sleep(ONE_SECOND); } catch (InterruptedException e) {	}
				tfTimer.setText(hours + ":" + minutes + ":" + seconds);
			}  // End if(!this.isPause)
			else {
				pauseSemaphore.release();
				try { Thread.sleep(1); } catch (InterruptedException e) {	}
			}
			
			try { terminateSemaphore.acquire();} catch (InterruptedException e) { }
		} // End While
	}


	public int getHours() {
		return hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setPause(boolean isPause) {
		try { pauseSemaphore.acquire();} catch (InterruptedException e) { }
		this.isPause = isPause;
		pauseSemaphore.release();
	}


	public void setTerminate(boolean terminate) {
		try { terminateSemaphore.acquire();} catch (InterruptedException e) { }
		this.terminate = terminate;
		terminateSemaphore.release();
	}
	
	


}
