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
 * A utility for obtaining SureLogic loggers. This class is thread-safe.
 */
public class SLLogger {

	/**
	 * Setting this system property allows easy configuration of the logging
	 * level. For example <code>-DSLLoggingLevel=FINE</code> will now show
	 * fine logging messages to the console and the file (the Eclipse handler
	 * always shows SEVERE, WARNING, and INFO).
	 */
	public static final Level LEVEL = Level.parse(System.getProperty(
			"SLLoggingLevel", "INFO"));

	/**
	 * Everyone can reuse the same instance of this formatter because the
	 * format() method uses no instance state.
	 */
	private static final AtomicReference<SLFormatter> f_formatter = new AtomicReference<SLFormatter>(
			new SLFormatter());

	/**
	 * A simple cache of loggers we have already configured.
	 */
	private static final Map<String, Logger> f_nameToLogger = new HashMap<String, Logger>();

	/**
	 * A list of the handlers we manage for logging.
	 */
	private static final List<Handler> f_handlers = new ArrayList<Handler>();

	/**
	 * Adds all the handlers to the given logger.
	 * <p>
	 * It is a precondition that a lock on this class be held when invoking this
	 * method.
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
		final ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(LEVEL);
		addHandler(ch);
		try {
			FileHandler fh = new FileHandler(System
					.getProperty("java.io.tmpdir")
					+ File.separator + "SureLogic_Log.txt", true);
			fh.setLevel(LEVEL);
			addHandler(fh);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Unable to create FileHandler object for SureLogic logger",
					e);
		}
	}

	/**
	 * Internal method to get a logger.
	 * <p>
	 * It is a precondition that a lock on this class be held when invoking this
	 * method.
	 * 
	 * @param name
	 *            the logger name.
	 * @return a suitable Logger.
	 */
	private static Logger getLoggerInternal(final String name) {
		assert name != null;

		Logger logger = f_nameToLogger.get(name);
		if (logger == null) {
			logger = Logger.getLogger(name);
			logger.setLevel(LEVEL);
			f_nameToLogger.put(name, logger); // add to cache

			/*
			 * Setup this logger for use.
			 */
			logger.setUseParentHandlers(false);
			addAllHandlersTo(logger);
		}
		return logger;
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
	 *            name for the logger, may not be <code>null</code>. All
	 *            names passed to this method become prefixed by
	 *            <code>com.surelogic</code>.
	 * @return a suitable Logger.
	 * @throws NullPointerException
	 *             if the name is null.
	 */
	public static synchronized Logger getLogger(final String name) {
		if (name == null)
			throw new NullPointerException("name must be non-null");
		final String loggerName = "com.surelogic"
				+ ("".equals(name) ? "" : "." + name);

		return getLoggerInternal(loggerName);

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

	/**
	 * Find or create a logger for a class using the class name as the named
	 * subsystem for the logger.
	 * 
	 * @param aClass
	 *            the class object, may not be <code>null</code>.
	 * @return a suitable Logger.
	 */
	public static synchronized Logger getLoggerFor(Class<?> aClass) {
		if (aClass == null)
			throw new NullPointerException("class must be non-null");
		final String className = aClass.getName();
		return getLoggerInternal(className);
	}
}
