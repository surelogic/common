package com.surelogic.common.logging;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility for obtaining SureLogic loggers.
 */
public class SLLogger {
	public static void main(String[] args) {
		Logger l = getLogger("foo");
		l.info("test of info");
		l.log(Level.WARNING, "test of warning", new IllegalArgumentException(
				"my wacky message"));
	}

	/**
	 * Everyone can reuse the same instance of this formatter because the
	 * format() method uses no instance state.
	 */
	public static final AtomicReference<SLFormatter> f_formatter = new AtomicReference<SLFormatter>(
			new SLFormatter());

	/**
	 * A simple cache of loggers we have already configured.
	 */
	public static final Map<String, Logger> f_nameToLogger = new HashMap<String, Logger>();

	/**
	 * A list of the handlers we manage for logging.
	 */
	public static final List<Handler> f_handlers = new ArrayList<Handler>();

	/**
	 * Adds all the handlers to the given logger. It is a precondition that a
	 * lock on this class be held when invoking this method.
	 * 
	 * @param logger
	 *            the logger to add handlers to.
	 */
	private static void addAllHandlersTo(final Logger logger) {
		for (Handler handler : f_handlers) {
			logger.addHandler(handler);
		}
	}

	/**
	 * Adds a handler to the set of handlers used by SureLogic logging.
	 * 
	 * @param handler
	 *            the handler to log.
	 * 
	 * @throws NullPointerException
	 *             if the handler is null.
	 */
	public static synchronized void addHandler(final Handler handler) {
		if (handler == null)
			throw new NullPointerException("handler must be non-null");

		handler.setFormatter(f_formatter.get());
		f_handlers.add(handler);

		/*
		 * Add this handler to all the existing loggers.
		 */
		for (Logger logger : f_nameToLogger.values()) {
			logger.addHandler(handler);
		}
	}

	static {
		addHandler(new ConsoleHandler());
		try {
			addHandler(new FileHandler(System.getProperty("java.io.tmpdir")
					+ File.separator + "SureLogic_Log.txt", true));
		} catch (Exception e) {
			throw new IllegalStateException(
					"Unable to create FileHandler object for SureLogic logger",
					e);
		}
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
	 * concise output format to the console (defined by {@link SLFormatter}) as
	 * well as to the file <code>SureLogic_Log.txt</code> in the directory
	 * specified by the <code>java.io.tmpdir</code> property. It will <i>not</i>
	 * send logging output to its parent's handlers. It will be registered in
	 * the LogManager global name space.
	 * 
	 * @param name
	 *            name for the logger. All names passed to this method become
	 *            prefixed by <code>com.surelogic</code>.
	 * @return a suitable Logger.
	 * @throws NullPointerException
	 *             if the name is null.
	 */
	public static synchronized Logger getLogger(final String name) {
		if (name == null)
			throw new NullPointerException("name must be non-null");
		final String loggerName = "com.surelogic"
				+ ("".equals(name) ? "" : "." + name);

		Logger logger = f_nameToLogger.get(loggerName);
		if (logger == null) {
			logger = Logger.getLogger(loggerName);
			f_nameToLogger.put(loggerName, logger); // add to cache

			/*
			 * Setup this logger for use.
			 */
			logger.setUseParentHandlers(false);
			addAllHandlersTo(logger);
		}
		return logger;
	}

	/**
	 * Convenience method to get the <code>com.surelogic</code> logger.
	 * Invoking this method is equivalent to invoking:
	 * 
	 * <pre>
	 * getLogger(&quot;&quot;)
	 * </pre>
	 * 
	 * @return a suitable Logger.
	 * 
	 */
	public static synchronized Logger getLogger() {
		return getLogger("");
	}
}
