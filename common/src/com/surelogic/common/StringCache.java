package com.surelogic.common;

import java.util.concurrent.*;

import com.surelogic.*;

/**
 * Caches Strings to remove duplicates
 * (may not always be unique)
 * 
 * @author Edwin
 */
@ThreadSafe
public class StringCache {
	private final ConcurrentMap<String,String> cache = new ConcurrentHashMap<String,String>();
	
    public String cache(final String s) {
    	if (s == null) {
    		return null;
    	}
        final String cached = cache.putIfAbsent(s, s);
        if (cached == null) {
            return s; // It got cached
        }
        return cached;
    }
}
