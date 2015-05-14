/*
 * Created on May 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.surelogic.common.util;

import java.util.*;

public class IteratorUtil {
  public static final Object noElement = new Object();

  public static <T> Iteratable<T> makeIteratable(Collection<T> allResults) {
    return new SimpleIteratable<>(allResults.iterator());
  }
}
