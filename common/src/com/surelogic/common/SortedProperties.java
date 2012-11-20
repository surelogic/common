package com.surelogic.common;

import java.util.*;

public class SortedProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Object> keySet(){
		return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
	}
	@Override
	public synchronized Enumeration<Object> keys() {
		return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	}
}
