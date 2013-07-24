package com.surelogic.common;

import java.util.Comparator;

import com.surelogic.Nullable;

public final class StringComparators {
  public static final Comparator<String> SORT_ALPHABETICALLY = String.CASE_INSENSITIVE_ORDER;

  public static final Comparator<String> SORT_NUMERICALLY = new Comparator<String>() {
    @Override
    public int compare(final String o1, final String o2) {
      final long i1 = Long.parseLong(o1);
      final long i2 = Long.parseLong(o2);
      if (i1 < i2) {
        return -1;
      } else if (i1 == i2) {
        return 0;
      } else {
        return 1;
      }
    }
  };

  public static final Comparator<String> SORT_NUMERICALLY_THEN_LEXICALLY = new Comparator<String>() {
    @Override
    public int compare(final String o1, final String o2) {
      final Long l1 = safeParseLong(o1);
      final Long l2 = safeParseLong(o2);
      if (l1 != null) {
        if (l2 != null) {
          return l1.compareTo(l2);
        } else {
          return -1;
        }
      } else if (l2 != null) {
        return 1;
      } else {
        return SORT_ALPHABETICALLY.compare(o1, o2);
      }
    }

  };

  /**
   * This method converts a string to a long but it ignores non-numeric
   * suffices. For example, invoking {@code safeParseLong("40 ns")} would result
   * in 40 (i.e., not an error). A {@code null} is returned if no numeric
   * portion exists.
   * 
   * @param value
   *          the string to convert.
   * @return the resulting long value. If the value is entirely non-numeric the
   *         result will be {@code null}.
   */
  @Nullable
  private static Long safeParseLong(String value) {
    if (value == null)
      return null;
    value = value.trim();
    long result = 0;
    boolean hasNumPart = false;
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      if (ch == ',' && hasNumPart)
        continue;
      final long digit = ch - '0';
      final boolean isNumeric = 0 <= digit && digit <= 9;
      if (isNumeric) {
        result = (result * 10) + digit;
        hasNumPart = true;
      } else
        break;
    }
    if (!hasNumPart) {
      return null;
    } else {
      return result;
    }
  }
}
