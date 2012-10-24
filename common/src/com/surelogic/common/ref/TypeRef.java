package com.surelogic.common.ref;

import java.util.ArrayList;
import java.util.List;

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
    final StringBuilder b = new StringBuilder(this.getClass().getSimpleName());
    b.append('[');
    b.append('\u00ab').append(getCompact()).append('\u00bb');
    b.append(getFullyQualified());
    b.append(']');
    return b.toString();
  }

  /**
   * Encodes this for persistence as a string. Use
   * {@link #parseEncodedForPersistence(String)} to return the string to a
   * {@link TypeRef}.
   * 
   * @return a string.
   */
  @NonNull
  String encodeForPersistence() {
    final StringBuilder b = new StringBuilder();
    b.append(f_compact).append(',').append(f_fullyQualified);
    return b.toString();
  }

  /**
   * Parses the result of {@link #encodeForPersistence()} back to a
   * {@link TypeRef}.
   * 
   * @param value
   *          a string.
   * @return a type reference.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  @NonNull
  static TypeRef parseEncodedForPersistence(final String value) {
    if (value == null)
      throw new IllegalArgumentException(I18N.err(44, "value"));
    final String v = value.trim();
    final int sepIndex = v.indexOf(",");
    if (sepIndex == -1)
      throw new IllegalArgumentException("Not an encoded TypeRef: " + v);
    final String compact = v.substring(0, sepIndex);
    final String fullyQualified = v.substring(sepIndex + 1);
    return new TypeRef(fullyQualified, compact);
  }

  /**
   * Encodes a list of type references for persistence as a string. Use
   * {@link #parseListEncodedForPersistence(String)} to return the string to a
   * list of {@link TypeRef}.
   * 
   * @param typeRefs
   *          a list of type references.
   * @return a string.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  @NonNull
  static String encodeListForPersistence(List<TypeRef> typeRefs) {
    if (typeRefs == null)
      throw new IllegalArgumentException(I18N.err(44, "typeRefs"));
    final StringBuilder b = new StringBuilder();
    if (typeRefs.isEmpty())
      b.append(";");
    else
      for (TypeRef ref : typeRefs) {
        b.append(ref.encodeForPersistence());
        b.append(";");
      }
    return b.toString();
  }

  /**
   * Parses the result of {@link #encodeListForPersistence(List)} back to a list
   * of {@link TypeRef}.
   * 
   * @param value
   *          a string.
   * @return a possibly empty list of type references.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  @NonNull
  static List<TypeRef> parseListEncodedForPersistence(final String value) {
    if (value == null)
      throw new IllegalArgumentException(I18N.err(44, "value"));
    final List<TypeRef> result = new ArrayList<TypeRef>();
    final StringBuilder b = new StringBuilder(value.trim());
    while (true) {
      final int sepIndex = b.indexOf(";");
      if (sepIndex == -1)
        break;
      final String encoded = b.substring(0, sepIndex).trim();
      if (encoded.length() < 1)
        break;
      result.add(parseEncodedForPersistence(encoded));
      b.delete(0, sepIndex + 1);
    }
    return result;
  }
}
