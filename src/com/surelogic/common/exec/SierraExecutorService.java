package com.surelogic.common.exec;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Instances of this handler accept UserTransactions to be executed at a later
 * time. The current implementation is single-threaded w/ a queue. This class
 * mainly serves to ease instantiation of the service in Jetty.
 * 
 * @author nathan
 * 
 */
public class SierraExecutorService implements ExecutorService {

	private final ExecutorService service;

	public SierraExecutorService() {
		service = Executors.newSingleThreadExecutor();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return service.awaitTermination(timeout, unit);
	}

	public void execute(Runnable command) {
		service.execute(command);
	}

	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return service.invokeAll(tasks, timeout, unit);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return service.invokeAll(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return service.invokeAny(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return service.invokeAny(tasks);
	}

	public boolean isShutdown() {
		return service.isShutdown();
	}

	public boolean isTerminated() {
		return service.isTerminated();
	}

	public void shutdown() {
		service.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return service.shutdownNow();
	}

	public <T> Future<T> submit(Callable<T> task) {
		return service.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return service.submit(task, result);
	}

	public Future<?> submit(Runnable task) {
		return service.submit(task);
	}

}
