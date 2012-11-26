package com.surelogic.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;

/**
 * Caches strings to attempt to alias references to the same string.
 * <p>
 * Clients should toss this instance so that the map contained
 */
@ThreadSafe
public class StringCache {
  private final ConcurrentMap<String, String> f_stringCache = new ConcurrentHashMap<String, String>();

  /**
   * This call tries to share, via an alias, strings if the string has been
   * previously passed to this method. It will never change the contents of the
   * string.
   * 
   * @param value
   *          a string
   * @return the same string, but perhaps aliased with other strings that are
   *         the same.
   */
  @Nullable
  public String aliasIfPossible(@Nullable final String value) {
    if (value == null)
      return null;
    
    final String cached = f_stringCache.putIfAbsent(value, value);
    if (cached == null)
      return value; // It got cached
    else
      return cached;
  }
}
