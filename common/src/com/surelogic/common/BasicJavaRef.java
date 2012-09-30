package com.surelogic.common;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

/**
 * Provides a basic implementation of this class that should be extended by
 * other implementations to get the details of the {@link IJavaRef} correct.
 */
@ThreadSafe
public class BasicJavaRef implements IJavaRef {
  @NonNull
  private final Within f_within;
  @NonNull
  private final String f_relativePath;

  public static void main(String[] args) {
    BasicJavaRef ref = new BasicJavaRef(Within.JAVA_FILE, "/java/lang/Object.class", "java.lang/Object", null);
    System.out.println(ref.getFileName());
  }

  /**
   * Matches JAR-style fully-qualified types. We use
   * {@link #f_typeNameFullyQualifiedJarStyle} to generate many results so we
   * want to be sure it is correctly formatted.
   * <p>
   * Examples: <tt>java.lang/Object</tt>,
   * <tt>java.util.collections/Map$Entry</tt>, <tt>/NotInAPkg</tt>
   */
  public static final String TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX = "([a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)*)*/[a-zA-Z]\\w*(\\$[a-zA-Z]\\w*)*";

  /**
   * A string that matches {@lin #TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX}.
   */
  @NonNull
  private final String f_typeNameFullyQualifiedJarStyle;
  @Nullable
  private final String f_eclipseProject;

  protected BasicJavaRef(final @NonNull Within within, final @NonNull String relativePath,
      final @NonNull String typeNameFullyQualifiedJarStyle, final @Nullable String eclipseProjectNameOrNull) {
    if (within == null)
      throw new IllegalArgumentException(I18N.err(44, "within"));
    f_within = within;
    if (relativePath == null)
      throw new IllegalArgumentException(I18N.err(44, "relativePath"));
    f_relativePath = relativePath;
    if (typeNameFullyQualifiedJarStyle == null)
      throw new IllegalArgumentException(I18N.err(44, "typeNameFullyQualifiedJarStyle"));
    if (!typeNameFullyQualifiedJarStyle.matches(TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX))
      throw new IllegalArgumentException(I18N.err(252, typeNameFullyQualifiedJarStyle));
    f_typeNameFullyQualifiedJarStyle = typeNameFullyQualifiedJarStyle;
    f_eclipseProject = eclipseProjectNameOrNull;
  }

  @NonNull
  public final Within getWithin() {
    return f_within;
  }

  public final boolean isFromSource() {
    return f_within == IJavaRef.Within.JAVA_FILE;
  }

  @Override
  @NonNull
  public String getRelativePath() {
    return f_relativePath;
  }

  @Override
  @NonNull
  public String getFileName() {
    int index = f_relativePath.lastIndexOf('/');
    if (index == -1)
      return f_relativePath;
    else
      return f_relativePath.substring(index);
  }

  @Override
  public int getLineNumber() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  @NonNull
  public String getEclipseProjectName() {
    if (f_eclipseProject == null)
      return SLUtility.UNKNOWN_PROJECT;
    else
      return f_eclipseProject;
  }

  @Override
  @NonNull
  public String getPackageName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public TypeType getTypeType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeNameFullyQualified() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @NonNull
  public String getTypeNameFullyQualifiedJarStyle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getJavaId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getEnclosingJavaId() {
    // TODO Auto-generated method stub
    return null;
  }
}
