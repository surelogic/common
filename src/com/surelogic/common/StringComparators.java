package com.surelogic.common;

import java.util.Comparator;

public final class StringComparators {
  public static final Comparator<String> SORT_ALPHABETICALLY = 
    String.CASE_INSENSITIVE_ORDER;
  
  public static final Comparator<String> SORT_NUMERICALLY = new Comparator<String>() {
    public int compare(String o1, String o2) {
      final long i1 = Long.parseLong(o1);
      final long i2 = Long.parseLong(o2);
      if (i1 < i2)
        return -1;
      else if (i1 == i2)
        return 0;
      else
        return 1;
    }
  };
}
