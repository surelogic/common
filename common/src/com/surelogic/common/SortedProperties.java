package com.surelogic.common;

import java.util.*;
import com.surelogic.ThreadSafe;
import com.surelogic.Starts;

@ThreadSafe
public class SortedProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Starts("nothing")
	@Override
	public Set<Object> keySet(){
		return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
	}
	@Starts("nothing")
	@Override
	public synchronized Enumeration<Object> keys() {
		return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	}
}
