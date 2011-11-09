package com.surelogic.common.jobs.console;

import java.io.PrintWriter;

import com.surelogic.*;
import com.surelogic.common.jobs.CancellableSLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLProgressMonitorFactory;

/**
 * A progress monitor that prints status reports to a {@link PrintWriter}.
 */
@Region("MonitorState")
@RegionLock("Lock is this protects MonitorState")
@InRegion("f_indentLevel, f_atNewLine into MonitorState")
public final class PrintWriterSLProgressMonitor extends
		CancellableSLProgressMonitor {

	private static final class Factory implements SLProgressMonitorFactory {
		private final PrintWriter printWriter;

		public Factory(final PrintWriter pw) {
			printWriter = pw;
		}

		public SLProgressMonitor createSLProgressMonitor(final String taskName) {
			return new PrintWriterSLProgressMonitor(printWriter, taskName);
		}
	}

	public static SLProgressMonitorFactory getFactory(PrintWriter out) {
		return new Factory(out);
	}

	private static final String INDENT = "\t";
	private static final String BEGIN = " [.";
	private static final String WORK = ".";
	private static final String DONE = "]";

	private final PrintWriter f_out;
	private final String f_name;
	private int f_indentLevel = 0;
	private boolean f_atNewLine = true;

	
	/**
	 * Constructs a new console progress monitor instance.
	 * 
	 * @param pw
	 *            a character stream to output progress to.
	 * @param name
	 *            the name of the job we are monitoring the progress of.
	 */
	@Unique("return")
	public PrintWriterSLProgressMonitor(final PrintWriter pw, final String name) {
		f_out = pw;
		f_name = name;
	}

	/**
	 * Constructs a new console progress monitor that sends its output to
	 * {@link System#out}.
	 * 
	 * @param name
	 *            the name of the job we are monitoring the progress of.
	 */
	@Unique("return")
	public PrintWriterSLProgressMonitor(final String name) {
		this(new PrintWriter(System.out), name);
	}

	private synchronized void indent() {
		for (int i = 0; i < f_indentLevel; i++) {
			f_out.print(INDENT);
		}
	}

	public synchronized void begin() {
		indent();
		f_out.print(f_name);
		f_out.print(BEGIN);
		f_out.flush();
		f_atNewLine = false;
	}

	public synchronized void begin(int totalWork) {
		indent();
		f_out.print(f_name);
		f_out.print(BEGIN);
		f_out.flush();
		f_atNewLine = false;
	}

	public synchronized void done() {
		// First close any open subtasks
		while (f_indentLevel > 0) {
			subTaskDone();
		}
		f_out.println(DONE);
		f_out.flush();
		f_atNewLine = true;
	}

	public synchronized void subTask(final String name) {
		if (!f_atNewLine)
			f_out.println();
		f_indentLevel += 1;
		indent();
		f_out.print(name);
		f_out.print(BEGIN);
		f_out.flush();
		f_atNewLine = false;
	}

	public synchronized void subTaskDone() {
		if (f_atNewLine)
			indent();
		f_out.println(DONE);
		f_out.flush();
		f_indentLevel -= 1;
		f_atNewLine = true;
	}

	public synchronized void worked(final int work) {
		if (f_atNewLine)
			indent();
		f_out.print(WORK);
		f_out.flush();
		f_atNewLine = false;
	}
}
