package com.surelogic.common.jdbc;

public interface KeyHandler<T> {
	/**
	 * Returns an in-order array of the names of the keys expected. This is
	 * necessary for some data sources.
	 * 
	 * @return
	 */
	String[] keyNames();

	T handle(Row r);
}
