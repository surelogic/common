package com.surelogic.common.concurrent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<T> extends AbstractSet<T> {
	private final ConcurrentHashMap<T,Object> map;
	
	public ConcurrentHashSet() {
		this(16);
	}
	public ConcurrentHashSet(int capacity) {
		map = new ConcurrentHashMap<T,Object>(capacity);
	}

	@Override
	public boolean add(T e) {
		map.put(e, e);
		return true;
	}
	
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
	
	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}
	
	@Override
	public int size() {
		return map.size();
	}	 
}
