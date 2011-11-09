package com.surelogic.common;

/**
 * This interface is implemented by objects that cache data. It provides an
 * operation to sweep the cache clean.
 */
public interface Sweepable {

	/**
	 * Performs periodic housekeeping by cleaning up cached data. This method
	 * may be safely called at any time, however, frequent calls may impact the
	 * targets ability to cache and therefore lower performance.
	 */
	void periodicSweep();
}
