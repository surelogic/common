package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

@ValueObject
public final class TypeRef {

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((f_compact == null) ? 0 : f_compact.hashCode());
    result = prime * result + ((f_fullyQualified == null) ? 0 : f_fullyQualified.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TypeRef))
      return false;
    TypeRef other = (TypeRef) obj;
    if (f_compact == null) {
      if (other.f_compact != null)
        return false;
    } else if (!f_compact.equals(other.f_compact))
      return false;
    if (f_fullyQualified == null) {
      if (other.f_fullyQualified != null)
        return false;
    } else if (!f_fullyQualified.equals(other.f_fullyQualified))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TypeRef(" + f_fullyQualified + ":" + f_compact + ")";
  }
}
