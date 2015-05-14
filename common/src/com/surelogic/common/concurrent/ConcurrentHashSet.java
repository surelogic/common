package com.surelogic.common.concurrent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.surelogic.*;

@ThreadSafe
@AnnotationBounds(referenceObject = {"T"}, threadSafe = {"T"})
public class ConcurrentHashSet<T> extends AbstractSet<T> {
	private final ConcurrentHashMap<T,T> map;
	
	public ConcurrentHashSet() {
		this(16);
	}
	public ConcurrentHashSet(int capacity) {
		map = new ConcurrentHashMap<>(capacity);
	}

	@Override
	public boolean add(T e) {
		map.put(e, e);
		return true;
	}
	
	@Starts("nothing")
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}
	
	@Starts("nothing")
	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}
	
	@Starts("nothing")
	@Override
	public int size() {
		return map.size();
	}	 
}
