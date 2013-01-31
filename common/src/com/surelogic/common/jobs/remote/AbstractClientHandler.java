package com.surelogic.common.jobs.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractClientHandler extends Thread implements IClientHandler {
	static AtomicInteger f_instanceCount = new AtomicInteger();
	
	protected volatile boolean f_shutdownRequested = false;

	final Console f_console;
	final Socket f_client;	

	public AbstractClientHandler(Console console, final Socket client) {
		super(console.getName()+"-client-handler "
				+ f_instanceCount.incrementAndGet());
		assert client != null;
		f_console = console;
		f_client = client;
	}

	@Override
	public void run() {
		try {
			// create input and output connections
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(f_client.getInputStream()));
			BufferedWriter outputStream = new BufferedWriter(
					new OutputStreamWriter(f_client.getOutputStream()));
			
			sendIntro(outputStream);			
			handleInput(inputStream, outputStream);
			f_console.log("console disconnect (" + getName() + ")");
		} catch (SocketException e) {
			/*
			 * ignore, this is normal behavior during a shutdown, i.e.,
			 * another thread has called f_client.close() via our
			 * requestShutdown() method.
			 */
		} catch (IOException e) {
			f_console.logAProblem("general I/O failure on socket used by "
					+ getName(), e);
		}
	}

	protected void sendIntro(BufferedWriter outputStream) {
		// Nothing to do yet
	}
	
	protected void handleInput(BufferedReader inputStream, BufferedWriter outputStream) throws IOException {
		while (!f_shutdownRequested) {
			String nextLine = inputStream.readLine(); // blocks
			if (nextLine == null) {
				/*
				 * This condition appears to occur when the client
				 * abruptly terminates its connection to the server (I
				 * found no documentation to support this, however). To
				 * make the server behave normally when an abrupt
				 * termination occurs, we need to "pretend" we received
				 * a quit command.
				 */
				f_shutdownRequested = true;
			} else {
				// process the command
				nextLine = nextLine.trim();
				handleInput(nextLine);
			}
		}
	}

	protected void handleInput(String line) {
		// Nothing to do yet
	}

	/**
	 * Sends the provided response String followed by a newline to the given
	 * output steam. Then {@link BufferedWriter#flush()} is called on the
	 * output steam.
	 * 
	 * @param outputStream
	 *            the stream to output to.
	 * @param response
	 *            the data to write to the stream.
	 */
	protected final void sendResponse(final BufferedWriter outputStream,
			final String response) {
		try {
			outputStream.write(response + "\n\r");
			outputStream.flush();
		} catch (IOException e) {
			f_console.logAProblem(
					"general I/O failure writing to socket used by "
							+ getName(), e);
		}
	}

	/**
	 * Signals that this client handler should be shutdown. This method
	 * returns immediately.
	 */
	@Override
  public void requestShutdown() {
		f_shutdownRequested = true;
		try {
			this.interrupt(); // wake up
			f_client.close();
		} catch (IOException e) {
			f_console.logAProblem("unable to close the socket used by "+getName(), e);
		}
	}
}
