package com.surelogic.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility with SureLogic common code.
 */
public final class SLUtility {

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
	public static int safeLongToInt(long value) {
		long result = Math.min(value, Integer.MAX_VALUE);
		return (int) result;
	}

	private final static ThreadLocal<SimpleDateFormat> tl_day_format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	public static synchronized String toStringDay(final Date date) {
		return tl_day_format.get().format(date);
	}

	private final static ThreadLocal<SimpleDateFormat> tl_hms_format = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public static synchronized String toStringHMS(final Date date) {
		return tl_hms_format.get().format(date);
	}

	public static Timestamp getWall(final Timestamp start, final long startNS,
			final long timeNS) {
		long tMS = start.getTime();
		final long deltaNS = timeNS - startNS;
		if (deltaNS < 0)
			throw new IllegalStateException("timeNS=" + timeNS
					+ " cannot be less than startedNS=" + startNS);
		final long deltaMS = deltaNS / 1000000;
		tMS = tMS + deltaMS;
		long tDecNS = (tMS % 1000) * 1000000;
		tDecNS = tDecNS + (deltaNS % 1000000);
		Timestamp result = new Timestamp(tMS);
		result.setNanos((int) tDecNS);
		return result;
	}

	private SLUtility() {
		// no instances
	}
}
