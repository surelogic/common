package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;

public final class TypeRef {

  public static final TypeRef[] EMPTY = new TypeRef[0];

  private final String f_fullyQualified;
  private final String f_compact;

  /**
   * Constructs a type use reference.
   * 
   * @param fullyQualified
   *          the fully qualified type use as a string, e.g.,
   *          <tt>java.lang.Object</tt>,
   *          <tt>java.util.List&lt;java.lang.String&gt;</tt>.
   * @param compact
   *          a compact representation of the type, e.g., <tt>Object</tt>,
   *          <tt>List&lt;String&gt;</tt>.
   */
  public TypeRef(final String fullyQualified, final String compact) {
    if (fullyQualified == null)
      throw new IllegalArgumentException(I18N.err(44, "fullyQualified"));
    f_fullyQualified = fullyQualified;
    if (compact == null)
      throw new IllegalArgumentException(I18N.err(44, "compact"));
    f_compact = compact;
  }

  /**
   * Gets the fully qualified type use as a string.
   * <p>
   * Examples: <tt>java.lang.Object</tt>,
   * <tt>java.util.List&lt;java.lang.String&gt;</tt>
   * 
   * @return the fully qualified type use as a string.
   */
  @NonNull
  public String getFullyQualified() {
    return f_fullyQualified;
  }

  /**
   * Gets a compact representation of the type use as a string&mdash;intended
   * for display in the UI.
   * <p>
   * Examples: <tt>Object</tt>, <tt>List&lt;String&gt;</tt>
   * 
   * @return a compact representation of the type use as a string.
   */
  @NonNull
  public String getCompact() {
    return f_compact;
  }
}
