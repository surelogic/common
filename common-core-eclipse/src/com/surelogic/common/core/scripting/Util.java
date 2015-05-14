package com.surelogic.common.core.scripting;

import java.util.*;

import com.surelogic.common.SLUtility;

public class Util {
  /**
   * Splits the line into tokens based on delim
   * 
   * @return non-null
   */
  public static String[] collectTokens(String line, String delim) {
    final StringTokenizer st = new StringTokenizer(line, delim);
    final List<String> l = new ArrayList<>();
    while (st.hasMoreTokens()) {
      l.add(st.nextToken());
    }
    if (l.isEmpty()) {
      return SLUtility.EMPTY_STRING_ARRAY;
    }
    return l.toArray(new String[l.size()]);
  }
}
