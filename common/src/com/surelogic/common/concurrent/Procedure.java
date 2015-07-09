package com.surelogic.common.concurrent;

/**
 * A {@code Procedure} accepts an argument but doesn't return a result. Example:
 * printing a value. An {@code Action} is a Procedure that takes no arguments.
 * <p>
 * Taken from <a href="http://g.oswego.edu/dl/concurrency-interest/">Doug Lea's
 * concurrency-interest web page</a> package extra166y.
 *
 * @param <A>
 *          the type of the argument passed to the {@code op} method.
 */
public interface Procedure<A> {
  void op(A a);
}
