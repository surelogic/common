package com.surelogic.common.util;

import java.util.Iterator;

public final class EmptyIteratable<T> implements Iteratable<T> {
  @SuppressWarnings("rawtypes")
  private static final EmptyIteratable prototype = new EmptyIteratable();
  
  private EmptyIteratable() {
    // TODO Auto-generated constructor stub
  }

  @SuppressWarnings({ "cast", "unchecked" })
  public static <T> EmptyIteratable<T> get() {
    return (EmptyIteratable<T>) prototype;
  }
  
  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public T next() {
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }
}
