package com.surelogic.common.jobs.remote;

import java.io.*;
import java.net.Socket;
import java.util.*;

import com.surelogic.common.jobs.remote.Console;

public abstract class AbstractLocalHandlerFactory<T extends AbstractLocalSLJob<?>, T2 extends ILocalConfig> 
implements IClientHandlerFactory {
	private final Console console;
	
	// Jobs waiting for their handlers
	private LinkedList<T> queue = new LinkedList<T>();
	
	protected AbstractLocalHandlerFactory(String name, int startingPort) {
		console = new Console(name, this, startingPort);
		
		if (startingPort > 0) {
			console.start();
		}
	}
	
	public final T newJob(String name, int work, T2 config) {
		T job = createJob(name, work, config, console);
		queue.add(job);
		return job;	
	}
	
	protected abstract T createJob(String name, int work, T2 config, Console console);

	@Override
	public final IClientHandler newHandler(Console console, Socket client) {
		if (console != this.console) {
			throw new IllegalStateException("Consoles don't match");
		}
		final T job = queue.removeFirst();
		return new AbstractClientHandler(console, client) {			
			@Override
			protected void handleInput(BufferedReader inputStream, BufferedWriter outputStream) {
				job.setHandlerThread(this);
				job.handleInput(inputStream, outputStream);
			}
		};
	}
}
