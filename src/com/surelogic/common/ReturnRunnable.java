package com.surelogic.common;

/**
 * A Runnable with a return value
 * @author Edwin.Chan
 */
public abstract class ReturnRunnable<T> implements Runnable {
  protected T value;
  
  public T getReturnValue() {
    return value;
  }
}
