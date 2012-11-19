package com.surelogic.common;

import java.io.*;

/**
 * Throws away the output
 * 
 * @author Edwin
 */
public final class NullOutputStream extends OutputStream {
	private NullOutputStream() {
		// Nothing to do
	}
	
	@Override
	public void write(int b) {
		// Does nothing
	}
	
	public static final NullOutputStream prototype = new NullOutputStream();
	public static final PrintStream out = new PrintStream(prototype);
}
