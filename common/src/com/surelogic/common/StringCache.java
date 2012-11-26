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

  @Nullable
  public String aliasIfPossible(@Nullable final String s) {
    if (s == null) {
      return null;
    }
    final String cached = f_stringCache.putIfAbsent(s, s);
    if (cached == null)
      return s; // It got cached
    else
      return cached;
  }
}
