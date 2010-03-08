package com.surelogic.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import org.apache.commons.lang.SystemUtils;

import com.surelogic.common.i18n.I18N;

/**
 * A utility with SureLogic common code.
 */
public final class SLUtility {
	public static final boolean is64bit = (SystemUtils.OS_ARCH.indexOf("64") >= 0);
	public static final String JAVA_DEFAULT_PACKAGE = "(default package)";

	/**
	 * Returns an {@code int} value of the passed {@code long} value or
	 * {@link Integer#MAX_VALUE} if the long is too bit to fit into an {@code
	 * int}.
	 * 
	 * @param value
	 *            the long value.
	 * @return an {@code int} value of the {@code long} value or
	 *         {@link Integer#MAX_VALUE} if the long is too bit to fit into an
	 *         {@code int}.
	 */
	public static int safeLongToInt(final long value) {
		final long result = Math.min(value, Integer.MAX_VALUE);
		return (int) result;
	}

	private final static ThreadLocal<SimpleDateFormat> tl_day_format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	public static String toStringDay(final Date date) {
		return tl_day_format.get().format(date);
	}

	public static Date fromStringDay(final String dateStr)
			throws ParseException {
		return tl_day_format.get().parse(dateStr);
	}

	private final static ThreadLocal<SimpleDateFormat> tl_hms_format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public static String toStringHMS(final Date date) {
		return tl_hms_format.get().format(date);
	}

	public static Date fromStringHMS(final String dateStr)
			throws ParseException {
		return tl_hms_format.get().parse(dateStr);
	}

	public static Timestamp getWall(final Timestamp start, final long startNS,
			final long timeNS) {
		long tMS = start.getTime();
		final long deltaNS = timeNS - startNS;
		if (deltaNS < 0) {
			throw new IllegalStateException("timeNS=" + timeNS
					+ " cannot be less than startedNS=" + startNS);
		}
		final long deltaMS = deltaNS / 1000000;
		tMS = tMS + deltaMS;
		long tDecNS = (tMS % 1000) * 1000000;
		tDecNS = tDecNS + (deltaNS % 1000000);
		final Timestamp result = new Timestamp(tMS);
		result.setNanos((int) tDecNS);
		return result;
	}

	public static String toCommaSepString(final int i) {
		return toCommaSepString((long) i);
	}

	public static String toCommaSepString(final long i) {
		final Formatter f = new Formatter();
		f.format("%,d", i);
		return f.toString();
	}

	public static long byteToMByte(final long value) {
		return value / 1024L / 1024L;
	}

	/**
	 * Gets the current maximum heap memory size in megabytes of this process.
	 * It uses the result provided by {@link Runtime#maxMemory()} to compute its
	 * result.
	 * 
	 * @return The maximum heap memory size in megabytes.
	 */
	public static int getCurrentMaxMemorySizeInMb() {
		final Runtime rt = Runtime.getRuntime();
		return SLUtility.safeLongToInt(SLUtility.byteToMByte(rt.maxMemory()));
	}

	/**
	 * Escapes a Java string so that it can be output as a quoted string literal
	 * and be legal Java code.
	 * <p>
	 * For example, The string <code>This "doggie" \ "kitty" is nice</code>
	 * would generate the string <code>This \"doggie\" \\ \"kitty\" is nice</code>.
	 * 
	 * @param s
	 *            the string to escape.
	 * @return the resulting string literal (not surrounded by double
	 *         quotations).
	 */
	public static String escapeJavaStringForQuoting(String s) {
		if (s == null)
			throw new IllegalArgumentException(I18N.err(44, "s"));
		final StringBuilder b = new StringBuilder(s);

		int index = 0;
		while (true) {
			final int length = b.length();
			if (index >= length)
				break;
			char c = b.charAt(index);
			if (c == '\\' || c == '\"') {
				b.insert(index, '\\');
				index++;
			}
			index++;
		}
		return b.toString();
	}
	
	private SLUtility() {
		// no instances
	}
}
