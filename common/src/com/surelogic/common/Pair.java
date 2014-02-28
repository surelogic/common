package com.surelogic.common;

/**
 * A simple pair, defined so we can define equality and hashCode.
 */
public class Pair<T1, T2> {
  private final T1 elem1;
  private final T2 elem2;
  // Could store a hash code?
  
  public static <T1, T2> Pair<T1, T2> getInstance(final T1 o1, final T2 o2) {
    return new Pair<T1, T2>(o1, o2);
  }

  public Pair(final T1 o1, final T2 o2) {
    elem1 = o1;
    elem2 = o2;
  }

  public final T1 first() {
    return elem1;
  }

  public final T2 second() {
    return elem2;
  }

  @Override
  public final boolean equals(final Object other) {
    if (!(other instanceof Pair)) {
      return false;
    }
    @SuppressWarnings("unchecked")
    final Pair<T1, T2> otherPair = (Pair<T1, T2>) other;
    if (elem1 == null && elem2 == null) {
      return (otherPair.elem1 == null && otherPair.elem2 == null);
    } else if (elem1 == null) {
      return (otherPair.elem1 == null && elem2.equals(otherPair.elem2));
    } else if (elem2 == null) {
      return (otherPair.elem2 == null && elem1.equals(otherPair.elem1));
    } else {
      return (elem1.equals(otherPair.elem1) && elem2.equals(otherPair.elem2));
    }
  }

  @Override
  public final int hashCode() {
    if (elem1 == null && elem2 == null) {
      return 0;
    } else if (elem1 == null) {
      return elem2.hashCode();
    } else if (elem2 == null) {
      return elem1.hashCode();
    } else {
      return elem1.hashCode() + elem2.hashCode();
    }
  }

  @Override
  public final String toString() {
    return "<" + firstToString(elem1) + ", " + secondToString(elem2) + '>';
  }
  
  protected String firstToString(final T1 v) {
    return v.toString();
  }
  
  protected String secondToString(final T2 v) {
    return v.toString();
  }
}
