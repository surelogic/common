package com.surelogic.common.jobs;

public final class SLProgressUtility {

	public static int safeLongToInt(long value) {
		long result = Math.min(value, Integer.MAX_VALUE);
		return (int) result;
	}

	private SLProgressUtility() {
		// no instances
	}
}
