package com.surelogic.common;

/**
 * Interface for objects that follow an initialization after construction, but
 * before use, and disposal after use.
 */
public interface ILifecycle {

	/**
	 * Actions required after construction, but before use.
	 */
	void init();

	/**
	 * Actions required after use.
	 */
	void dispose();
}
