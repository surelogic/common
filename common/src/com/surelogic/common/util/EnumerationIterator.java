package com.surelogic.common.util;

import java.util.*;

/**
 * An Iterator created around an underlying 
 * {@link Enumeration}.  The {@link java.util.Iterator#remove()}
 * operation is not supported.
 */

public class EnumerationIterator<T>
extends AbstractRemovelessIterator<T>
{
  /** Enumeration to wrap. */
  private final Enumeration<T> enumeration;

  /**
   * Create a new iterator wrapped around an
   * existing enumeration.
   * @param enum The enumeration to wrap.
   */
  public EnumerationIterator( final Enumeration<T> enm )
  {
    enumeration = enm;
  }

  @Override
  public boolean hasNext()
  {
    return enumeration.hasMoreElements();
  }

  @Override
  public T next()
  {
    return enumeration.nextElement();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> Iteratable<T> create(Enumeration<T> e) {
	 return new EnumerationIterator(e);
  }
}
