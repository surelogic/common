package com.surelogic.common;

import java.util.concurrent.*;

/**
 * Hardcoded to check once per second
 * 
 * @author Edwin
 */
public class PeriodicUtility {
	private static final CopyOnWriteArraySet<Runnable> handlers = 
		new CopyOnWriteArraySet<Runnable>();
	
	static {
		final Runnable r = new Runnable() {
			public void run() {
				// constantly check for cancellation
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// we can ignore the unlikely case where we are
						// interrupted
					}
					for(Runnable h : handlers) {
						h.run();
					}
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	public static void addHandler(Runnable h) {
		handlers.add(h);
	}
}