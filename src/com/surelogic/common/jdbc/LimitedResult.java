package com.surelogic.common.jdbc;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a list of results, and indicates whether or not it is the complete
 * set.
 * 
 * @author nathan
 * 
 * @param <T>
 */
public class LimitedResult<T> implements List<T> {

    private final List<T> result;
    private final boolean hasMore;

    public LimitedResult(final List<T> result, final boolean hasMore) {
        this.result = result;
        this.hasMore = hasMore;
    }

    public List<T> getResult() {
        return result;
    }

    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public int size() {
        return result.size();
    }

    @Override
    public boolean isEmpty() {
        return result.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return result.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return result.iterator();
    }

    @Override
    public Object[] toArray() {
        return result.toArray();
    }

    @Override
    public <A> A[] toArray(final A[] a) {
        return result.toArray(a);
    }

    @Override
    public boolean add(final T e) {
        return result.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return result.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return result.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return result.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        return result.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return result.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return result.retainAll(c);
    }

    @Override
    public void clear() {
        result.clear();
    }

    @Override
    public boolean equals(final Object o) {
        return result.equals(o);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }

    @Override
    public T get(final int index) {
        return result.get(index);
    }

    @Override
    public T set(final int index, final T element) {
        return result.set(index, element);
    }

    @Override
    public void add(final int index, final T element) {
        result.add(index, element);
    }

    @Override
    public T remove(final int index) {
        return result.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return result.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return result.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return result.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
        return result.listIterator(index);
    }

    @Override
    public List<T> subList(final int fromIndex, final int toIndex) {
        return result.subList(fromIndex, toIndex);
    }

}
