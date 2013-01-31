package com.surelogic.common.concurrent;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import org.apache.commons.collections15.MultiMap;

import com.surelogic.*;

/**
 * A thread-safe version of MultiHashMap
 * (note: no need for putIfAbsent, since we're not keeping the values unique)
 * 
 * @author Edwin
 */
@ThreadSafe
@AnnotationBounds(referenceObject = {"K", "V"}, threadSafe = {"K", "V"})
public class ConcurrentMultiHashMap<K,V> implements MultiMap<K,V> {
	@Vouch("ThreadSafe")
	private final ConcurrentMap<K,Collection<V>> map = new ConcurrentHashMap<K,Collection<V>>();

	private Collection<V> getOrEmpty(Object key) {
		Collection<V> values = map.get(key);
		if (values == null) {
			return Collections.emptyList();
		}
		return values;
	}
	
	@Override
  public void clear() {
		map.clear();
	}

	@Override
  public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
  public boolean containsValue(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
  public boolean containsValue(Object key, Object value) {
		return getOrEmpty(key).contains(value);
	}

	@Override
  public Set<Entry<K, Collection<V>>> entrySet() {
		return map.entrySet();
	}

	@Override
  public Collection<V> get(Object key) {
		return map.get(key);
	}

	@Override
  public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
  public Iterator<V> iterator(Object key) {
		return getOrEmpty(key).iterator();
	}

	@Override
  public Set<K> keySet() {
		return map.keySet();
	}

	@Override
  public Map<K, Collection<V>> map() {
		throw new UnsupportedOperationException();
	}

	private Collection<V> ensureCollection(K key) {
		Collection<V> values = map.get(key);
		if (values == null) {
			values = newCollection();
			Collection<V> tempValues = map.putIfAbsent(key, values);
			if (tempValues != null) {
				values = tempValues;
			}
		}
		return values;
	}
	
	/**
	 * Can be overridden to change how it responded to duplicates
	 */
	protected Collection<V> newCollection() {
		return new Vector<V>(1);
	}
	
	@Override
  public V put(K key, V value) {
		Collection<V> values = ensureCollection(key);
		values.add(value);
		return value; // Always changed here
	}

	@Override
  public void putAll(Map<? extends K, ? extends V> m) {
		for(Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	@Override
  public void putAll(MultiMap<? extends K, ? extends V> mm) {
		for(Entry<? extends K, ? extends Collection<? extends V>> e : mm.entrySet()) {
			putAll(e.getKey(), e.getValue());
		}		
	}

	@Override
  public boolean putAll(K key, Collection<? extends V> newValues) {
		Collection<V> values = ensureCollection(key);
		values.addAll(newValues);
		return !newValues.isEmpty();
	}

	@Override
  public Collection<V> remove(Object key) {
		return map.remove(key);
	}

	@Override
  @SuppressWarnings("unchecked")
	public V remove(Object key, Object value) {
		boolean success = getOrEmpty(key).remove(value);
		return success ? (V) value : null;
	}

	@Override
  public int size() {
		return map.size();
	}

	@Override
  public int size(Object key) {
		return getOrEmpty(key).size();
	}

	@Override
  public Collection<V> values() {
		throw new UnsupportedOperationException();
	}
}
