package com.surelogic.common;

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

	private SLUtility() {
		// no instances
	}
}
