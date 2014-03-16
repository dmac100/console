package jsconsole.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A Runnable that will execute a wrapped runnable at most once every 'wait' milliseconds. The
 * first call will execute immediately, while any other calls during the wait period will cause
 * a single call to the runnable after the wait is over.
 */
public class ThrottledRunnable implements Runnable {
	private final long wait;
	private final Runnable runnable;
	
	private Timer timer = null;
	private long lastTime = 0;
	
	public ThrottledRunnable(Runnable runnable, long wait) {
		this.runnable = runnable;
		this.wait = wait;
	}
	
	public synchronized void run() {
		if(timer != null) return;
		
		long remainingTime = wait - (System.currentTimeMillis() - lastTime);
		
		if(remainingTime < 0) {
			lastTime = System.currentTimeMillis();
			runnable.run();
		} else {
			timer = new Timer();
			
			timer.schedule(new TimerTask() {
				public void run() {
					onTimer();
				}
			}, Math.min(remainingTime, wait));
		}
	}
	
	private void onTimer() {
		synchronized(this) {
			lastTime = System.currentTimeMillis();
			timer.cancel();
			timer = null;
		}
		
		runnable.run();
	}
}
