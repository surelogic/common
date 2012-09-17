package com.surelogic.common.i18n;

import com.surelogic.NonNull;

/**
 * A class that stores a reference to a Java source reference. The project name
 * and line number are optional. The package name and type name are required.
 * This is a value object.
 * <p>
 * Use {@link #UNKNOWN} if an instance is needed and a source reference is not
 * available.
 */
public final class JavaSourceReference {

  public static final JavaSourceReference UNKNOWN = new JavaSourceReference();

  /**
   * Optional, may be null.
   */
  private final String f_projectName;

  @NonNull
  private final String f_packageName;

  @NonNull
  private final String f_typeName;

  /**
   * Must be non-negative. 0 indicates that the line number is not known.
   */
  private final int f_lineNumber;

  /**
   * -1 indicates that the offset is not known
   */
  private final int f_offset;

  public JavaSourceReference(String projectName, String packageName, String typeName, int lineNumber, int offset) {
    f_projectName = projectName;
    if (packageName == null) {
      packageName = "(default)";
    }
    f_packageName = packageName;
    if (typeName == null)
      throw new IllegalArgumentException(I18N.err(44, "typeName"));
    f_typeName = typeName;
    if (lineNumber < 0)
      lineNumber = 0;
    f_lineNumber = lineNumber;

    if (offset < -1) {
      offset = -1;
    }
    f_offset = offset;
  }

  public JavaSourceReference(String projectName, String packageName, String typeName) {
    this(projectName, packageName, typeName, 0, -1);
  }

  public JavaSourceReference(String packageName, String typeName, int lineNumber, int offset) {
    this(null, packageName, typeName, lineNumber, offset);
  }

  public JavaSourceReference(String packageName, String typeName) {
    this(packageName, typeName, 0, -1);
  }

  /**
   * Creates an unknown source reference.
   */
  private JavaSourceReference() {
    this(null, null, "unknown", 0, 0);
  }

  public String getProjectName() {
    return f_projectName;
  }

  @NonNull
  public String getPackageName() {
    return f_packageName;
  }

  @NonNull
  public String getTypeName() {
    return f_typeName;
  }

  public int getLineNumber() {
    return f_lineNumber;
  }

  public int getOffset() {
    return f_offset;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime + f_offset;
    result = prime * result + f_lineNumber;
    result = prime * result + ((f_packageName == null) ? 0 : f_packageName.hashCode());
    result = prime * result + ((f_projectName == null) ? 0 : f_projectName.hashCode());
    result = prime * result + ((f_typeName == null) ? 0 : f_typeName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JavaSourceReference other = (JavaSourceReference) obj;
    if (f_lineNumber != other.f_lineNumber)
      return false;
    if (f_offset != other.f_offset) {
      return false;
    }
    if (f_packageName == null) {
      if (other.f_packageName != null)
        return false;
    } else if (!f_packageName.equals(other.f_packageName))
      return false;
    if (f_projectName == null) {
      if (other.f_projectName != null)
        return false;
    } else if (!f_projectName.equals(other.f_projectName))
      return false;
    if (f_typeName == null) {
      if (other.f_typeName != null)
        return false;
    } else if (!f_typeName.equals(other.f_typeName))
      return false;
    return true;
  }

  /**
   * Returns an <i>n</i>-tuple containing the contents of this object. The
   * result is of the form
   * (<i>projectName</i>,<i>packageName</i>,</i>typeName</i>,<i>lineNumber</i>).
   * 
   * If the project name is {@code null} it is not included. If the line number
   * is 0 (meaning unknown) it is not included.
   * 
   * @return an <i>n</i>-tuple containing the contents of this object.
   */
  @NonNull
  public String toStringCanonical() {
    final StringBuilder b = new StringBuilder();
    b.append('(');
    final String projectName = getProjectName();
    if (projectName != null)
      b.append(projectName).append(',');
    b.append(getPackageName()).append(',');
    b.append(getTypeName());
    final int lineNumber = getLineNumber();
    if (lineNumber > 0)
      b.append(',').append(lineNumber);
    final int offset = getOffset();
    if (offset >= 0)
      b.append(',').append(offset);
    b.append(')');
    return b.toString();
  }

  /**
   * Returns a human readable message about the contents of this object. For
   * example, the string <tt>"at line 50 in com.surelogic.Foo (common)"</tt>
   * would result if this references line 50 of the class {@code Foo} in package
   * {@code com.surelogic} in the project {@code common}.
   * 
   * @return a human readable message about the contents of this object.
   */
  @NonNull
  public String toStringMessage() {
    final StringBuilder b = new StringBuilder();
    final int lineNumber = getLineNumber();
    if (lineNumber > 0) {
      b.append("at line ").append(lineNumber).append(' ');
    }
    b.append("in ");
    b.append(getPackageName()).append('.').append(getTypeName());
    final String projectName = getProjectName();
    if (projectName != null)
      b.append(" (").append(projectName).append(')');
    return b.toString();
  }

  @Override
  @NonNull
  public String toString() {
    return getClass().getName() + '@' + toStringCanonical();
  }
}
