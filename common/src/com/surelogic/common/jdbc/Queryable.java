package com.surelogic.common.jdbc;

/**
 * A Queryable object
 * 
 * @param <T>
 */
public interface Queryable<T> {

  T call(Object... args);

  void finished();
}
