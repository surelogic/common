package com.surelogic.common.jdbc;

/**
 * A Queryable object
 * 
 * @author nathan
 * 
 * @param <T>
 */
public interface Queryable<T> {

	T call(Object... args);

	void finished();
}