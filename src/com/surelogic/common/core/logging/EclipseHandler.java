package com.surelogic.common.core.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import com.surelogic.common.core.Activator;
import com.surelogic.common.logging.SLFormatter;
import com.surelogic.common.logging.SLLogger;

/**
 * A <code>java.util.logging.Handler</code> subclass that logs to the Eclipse
 * log. This handler ignores any formatter that is set for it.
 * <p>
 * This handler is effected by the Eclipse platform debug tracing facility
 * configuration in the <tt>.options</tt> file for this plug-in. For example,
 * the <tt>.options</tt> file
 * 
 * <pre>
 * com.surelogic.common.core/fine=false
 * com.surelogic.common.core/finer=true
 * com.surelogic.common.core/finest=false
 * </pre>
 * 
 * would cause all {@link Level#INFO} and {@link Level#FINER} messages to be
 * directed into the Eclipse log.
 */
public final class EclipseHandler extends Handler {

	static private final Level TRACE_LEVEL;

	static {
		final boolean logFine = "true".equalsIgnoreCase(Platform
				.getDebugOption("com.surelogic.common.core/fine"));
		final boolean logFiner = "true".equalsIgnoreCase(Platform
				.getDebugOption("com.surelogic.common.core/finer"));
		final boolean logFinest = "true".equalsIgnoreCase(Platform
				.getDebugOption("com.surelogic.common.core/finest"));

		if (logFinest)
			TRACE_LEVEL = Level.FINEST;
		else if (logFiner)
			TRACE_LEVEL = Level.FINER;
		else if (logFine)
			TRACE_LEVEL = Level.FINE;
		else
			TRACE_LEVEL = Level.INFO;
	}

	public EclipseHandler() {
		SLLogger.setLevel(TRACE_LEVEL);
	}

	@Override
	public void close() throws SecurityException {
		// nothing to do
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public void publish(final LogRecord record) {
		if (record == null)
			return;
		if (!isLoggable(record))
			return;

		final String message = record.getMessage() == null ? "" : record
				.getMessage();
		final StringBuilder b = new StringBuilder();
		SLFormatter.formatMsgTail(b, message, record.getSourceClassName(),
				record.getSourceMethodName());

		final Level level = record.getLevel();

		if (level == Level.SEVERE) {
			final int code = tryToExtractServiceabilityNumber(b.toString());
			log(SLEclipseStatusUtility.createErrorStatus(code, b.toString(),
					record.getThrown()));
		} else if (level == Level.WARNING) {
			final int code = tryToExtractServiceabilityNumber(b.toString());
			log(SLEclipseStatusUtility.createWarningStatus(code, b.toString(),
					record.getThrown()));
		} else {
			b.insert(0, "(" + level.toString() + ") ");
			log(SLEclipseStatusUtility.createInfoStatus(b.toString(),
					record.getThrown()));
		}
	}

	private void log(final IStatus status) {
		if (Activator.getDefault() != null) {
			Activator.getDefault().getLog().log(status);
		} else {
			System.err.println(status.getCode() + ": " + status.getMessage());
		}
	}

	/**
	 * Tries to extract the serviceability number from the message provided. Our
	 * numbered error messages start with <tt>(SureLogic #50) This ...</tt> so
	 * we can try to extract the number between the first '#' and the first ')'
	 * in the string.
	 * <p>
	 * This method should accept any string and not throw an exception.
	 * 
	 * @param message
	 *            the message to extract the serviceability number from, may be
	 *            <code>null</code>.
	 * @return the serviceability number, or 0 if our attempt failed.
	 */
	private int tryToExtractServiceabilityNumber(final String message) {
		int code = 0;
		if (message != null) {
			final int startI = message.indexOf('#');
			final int endI = message.indexOf(')');
			if (startI != -1 && endI != -1 && endI > startI) {
				try {
					String codeString = message.substring(startI + 1, endI);
					code = Integer.parseInt(codeString);
				} catch (Exception e) {
					/*
					 * Ignore this exception because we want to return the
					 * default code of 0.
					 */
				}
			}
		}
		return code;
	}
}
