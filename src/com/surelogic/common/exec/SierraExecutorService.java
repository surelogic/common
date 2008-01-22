package com.surelogic.common.exec;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Instances of this handler accept UserTransactions to be executed at a later
 * time. The current implementation is single-threaded w/ a queue. This class
 * mainly serves to ease instantiation of the service in Jetty.
 * 
 * @author nathan
 * 
 */
public class SierraExecutorService extends ThreadPoolExecutor {

	public SierraExecutorService() {
		super(1, 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

}
