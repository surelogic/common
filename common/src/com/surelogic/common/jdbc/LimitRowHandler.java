package com.surelogic.common.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link RowHandler} that will only pull a certain number of rows of data. It
 * may produce fewer if the number of rows available is less than the limit.
 * 
 * @author nathan
 * 
 * @param <T>
 */
public final class LimitRowHandler<T> implements ResultHandler<LimitedResult<T>> {

  private final RowHandler<T> handler;
  private final int limit;

  private LimitRowHandler(final RowHandler<T> handler, final int limit) {
    this.handler = handler;
    this.limit = limit;
  }

  @Override
  public LimitedResult<T> handle(final Result result) {
    int count = 0;
    List<T> resultList = new ArrayList<>();
    for (Row r : result) {
      if (count++ < limit) {
        resultList.add(handler.handle(r));
      }
    }
    return new LimitedResult<>(resultList, count);
  }

  /**
   * Constructs a handler that will handle at most the given number of rows.
   * 
   * @param <T>
   * @param handler
   *          the handler used to produce values
   * @param limit
   *          the maximum number of values to produce
   * @return
   */
  public static <T> ResultHandler<LimitedResult<T>> from(final RowHandler<T> handler, final int limit) {
    return new LimitRowHandler<>(handler, limit);
  }

}
