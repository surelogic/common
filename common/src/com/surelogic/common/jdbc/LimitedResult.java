package com.surelogic.common.jdbc;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import com.surelogic.Starts;

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
    private final int fullCount;

    public LimitedResult(final List<T> result, final int extraCount) {
        this.result = result;
        this.fullCount = extraCount;
    }

    public List<T> getResult() {
        return result;
    }

    public boolean isLimited() {
        return fullCount > result.size();
    }

    public int getFullCount() {
        return fullCount;
    }

    @Starts("nothing")
	@Override
    public int size() {
        return result.size();
    }

    @Starts("nothing")
	@Override
    public boolean isEmpty() {
        return result.isEmpty();
    }

    @Starts("nothing")
	@Override
    public boolean contains(final Object o) {
        return result.contains(o);
    }

    @Starts("nothing")
	@Override
    public Iterator<T> iterator() {
        return result.iterator();
    }

    @Starts("nothing")
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

    @Starts("nothing")
	@Override
    public boolean remove(final Object o) {
        return result.remove(o);
    }

    @Starts("nothing")
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

    @Starts("nothing")
	@Override
    public boolean removeAll(final Collection<?> c) {
        return result.removeAll(c);
    }

    @Starts("nothing")
	@Override
    public boolean retainAll(final Collection<?> c) {
        return result.retainAll(c);
    }

    @Starts("nothing")
	@Override
    public void clear() {
        result.clear();
    }

    @Starts("nothing")
	@Override
    public boolean equals(final Object o) {
        return result.equals(o);
    }

    @Starts("nothing")
	@Override
    public int hashCode() {
        return result.hashCode();
    }

    @Starts("nothing")
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

    @Starts("nothing")
	@Override
    public T remove(final int index) {
        return result.remove(index);
    }

    @Starts("nothing")
	@Override
    public int indexOf(final Object o) {
        return result.indexOf(o);
    }

    @Starts("nothing")
	@Override
    public int lastIndexOf(final Object o) {
        return result.lastIndexOf(o);
    }

    @Starts("nothing")
	@Override
    public ListIterator<T> listIterator() {
        return result.listIterator();
    }

    @Starts("nothing")
	@Override
    public ListIterator<T> listIterator(final int index) {
        return result.listIterator(index);
    }

    @Starts("nothing")
	@Override
    public List<T> subList(final int fromIndex, final int toIndex) {
        return result.subList(fromIndex, toIndex);
    }

    public int getExtraCount() {
        return getFullCount() - size();
    }

}
