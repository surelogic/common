package com.surelogic.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SLLogger extends Formatter {

	public static final boolean f_useSLLogger = System
			.getProperty("no-SLLogger") == null;

	/**
	 * Everyone can reuse the same instance of this formatter because the
	 * format() method uses no instance state.
	 */
	public static final SLLogger f_formatter;

	/**
	 * A simple cache of loggers we have already configured.
	 */
	public static final Map<String, Logger> f_nameToLogger;

	static {
		if (f_useSLLogger) {
			f_formatter = new SLLogger();
			f_nameToLogger = new HashMap<String, Logger>();
		} else {
			f_formatter = null;
			f_nameToLogger = null;
		}
	}

	public static void addHandler(final Handler handler) {
		handler.setFormatter(f_formatter);

	}

	/**
	 * Find or create a logger for a named subsystem. All names passed to this
	 * method become prefixed by <code>com.surelogic</code>. For example if
	 * the code is <code>SLLogger.getLogger("flashlight")</code> the name of
	 * the returned logger is <code>com.surelogic.flashlight</code>. If a
	 * logger has already been created with the given name it is returned.
	 * Otherwise a new logger is created.
	 * <p>
	 * If a new logger is created its log level will be configured to use a
	 * concise output format to the console (defined by this class) and it will
	 * <i>not</i> send logging output to its parent's handlers. It will be
	 * registered in the LogManager global name space.
	 * <P>
	 * if the system property <tt>no-fluid-logger</tt> was set as a virtual
	 * machine argument then this method is equivalent to invoking:
	 * 
	 * <pre>
	 * java.util.logging.Logger.getLogger(name);
	 * </pre>
	 * 
	 * @param name
	 *            name for the logger. This should be a dot-separated name and
	 *            should normally be based on the package name or class name of
	 *            the subsystem, such as java.net or javax.swing
	 * @return a suitable Logger
	 * @throws NullPointerException
	 *             if the name is null
	 */
	public static Logger getLogger(final String name) {
		if (name == null)
			throw new NullPointerException("name must be non-null");

		if (f_useSLLogger) {
			synchronized (f_nameToLogger) {
				Logger resultLogger = f_nameToLogger.get(name);
				if (resultLogger == null) {
					/*
					 * Setup this logger for use.
					 */
					resultLogger = Logger.getLogger(name);
					resultLogger.setUseParentHandlers(false);

					Handler consoleHandler = new ConsoleHandler();
					consoleHandler.setFormatter(f_formatter);
					resultLogger.addHandler(consoleHandler);

					// Output the log to a file in temp directory
					try {
						String tempDir = System.getProperty("java.io.tmpdir");
						Handler fileHandler = new FileHandler(tempDir
								+ File.separator + "sierraLog.txt", true);
						fileHandler.setFormatter(f_formatter);
						resultLogger.addHandler(fileHandler);

					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					f_nameToLogger.put(name, resultLogger); // add to cache
				}
				return resultLogger;
			}
		} else {
			return Logger.getLogger(name);
		}
	}

	/**
	 * Format the given log record and return the formatted string. The
	 * resulting formatted String for a ConciseFormatter outputs single lines
	 * using the following pattern:
	 * <p>[ <i>LEVEL </i>" <i>thread name </i>"] <i>message </i>[
	 * <i>package.class.method() </i>]
	 * <p>
	 * The {@link #formatMessage(LogRecord)} convenience method is used to
	 * localize and format the message field.
	 * 
	 * @param record
	 *            the log record to be formatted.
	 * @return the formatted log record.
	 */
	@Override
	public String format(final LogRecord record) {
		StringBuilder buf = new StringBuilder(1000);
		buf.append("[");
		buf.append(record.getLevel().getName());
		buf.append(" \"");
		buf.append(Thread.currentThread().getName());
		buf.append("\" ");
		buf.append(Calendar.getInstance().getTime().toString());
		buf.append("]");
		buf.append(formatMessage(record));
		buf.append(" [");
		buf.append(record.getSourceClassName());
		buf.append(".");
		buf.append(record.getSourceMethodName());
		buf.append("()]");
		buf.append('\n');
		Throwable t = record.getThrown();
		if (t != null) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			buf.append(sw.toString());
		}
		return buf.toString();
	}
}
