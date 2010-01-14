package com.surelogic.common.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.surelogic.RegionEffects;
import com.surelogic.Unique;

/**
 * A concise format for SureLogic loggers.
 */
public final class SLFormatter extends Formatter {
	@Unique("return")
	public SLFormatter() {
		// Only to be annotated
	}
	
	private final static ThreadLocal<SimpleDateFormat> tl_format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	/**
	 * Format the given log record and return the formatted string. The
	 * resulting formatted String outputs single lines using the following
	 * pattern:
	 * <p>[<i>yyyy-MM-dd HH:mm:ss</i> <i>LEVEL</i>] <i>message</i> [<i>package.class.method()</i>
	 * in <i>thread name</i>]
	 * <p>
	 * If the given log record contains an exception then the stack trace
	 * reported by that exception is added to the above as subsequent lines.
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
		final StringBuilder b = new StringBuilder();
		b.append("[").append(tl_format.get().format(new Date()));
		final String level = record.getLevel().getName();
		final int padding = 8 - level.length();
		for (int i = 0; i < padding; i++)
			b.append(" ");
		b.append(record.getLevel().getName()).append("] ");

		formatMsgTail(b, formatMessage(record), record.getSourceClassName(),
				record.getSourceMethodName());

		b.append(System.getProperty("line.separator"));
		final Throwable t = record.getThrown();
		if (t != null) {
		  final StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			b.append(sw.toString());
		}
		return b.toString();
	}

	/*
	 * This tail format code is used by this formatter as well as the Eclipse
	 * handler, therefore it is broken out into this utility method.
	 */
	public static void formatMsgTail(final StringBuilder b,
			final String message, final String className,
			final String methodName) {
		b.append(message).append(" (in method ");
		b.append(className).append(".").append(methodName).append(
				"(-) thread \"");
		b.append(Thread.currentThread().getName()).append("\")");
	}
}
