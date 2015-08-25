package com.surelogic.common.jdbc;

/**
 * Represents a query on a database.
 * 
 * @param <T>
 *          the type of the result from the query. Use {@link Void} if none.
 */
public interface DBQuery<T> {
  T perform(Query q);
}
