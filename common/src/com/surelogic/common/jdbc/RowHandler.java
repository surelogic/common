package com.surelogic.common.jdbc;

/**
 * An interface to handle rows returned from an SQL query and return a value for
 * each row. If you don't need a return for each row you should extend
 * {@link NullRowHandler} rather than directly implement this interface.
 * 
 * @param <T>
 *          the return type for each row. This is used to pass a value out of
 *          each row.
 * 
 * @see NullRowHandler
 */
public interface RowHandler<T> {

  /**
   * Process the passed row and return a value.
   * 
   * @param r
   *          a query result row.
   * @return a value.
   */
  T handle(Row r);
}
