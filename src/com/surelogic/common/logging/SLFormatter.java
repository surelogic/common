package com.surelogic.common.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A concise format for SureLogic loggers.
 */
public final class SLFormatter extends Formatter {

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
		StringBuilder buf = new StringBuilder(1000);
		buf.append("[").append(tl_format.get().format(new Date()));
		String level = record.getLevel().getName();
		int padding = 8 - level.length();
		for (int i = 0; i < padding; i++)
			buf.append(" ");
		buf.append(record.getLevel().getName()).append("] ");

		buf.append(formatMessage(record));

		buf.append(" [").append(record.getSourceClassName()).append(".")
				.append(record.getSourceMethodName()).append("() in \"");
		buf.append(Thread.currentThread().getName()).append("\"]");
		buf.append(System.getProperty("line.separator"));
		Throwable t = record.getThrown();
		if (t != null) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			buf.append(sw.toString());
		}
		return buf.toString();
	}
}
