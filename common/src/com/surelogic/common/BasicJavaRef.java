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

  public static final class Builder {

    private Within f_within = Within.JAVA_FILE;
    private String f_typeNameFullyQualifiedJarStyle;
    private String f_relativePath;
    private TypeType f_typeType = TypeType.CLASS;
    private String f_eclipseProjectName;
    private int f_lineNumber;
    private int f_offset;
    private int f_length;
    private String f_javaId;
    private String f_enclosingJavaId;

    public Builder(@NonNull String typeNameFullyQualifiedJarStyle) {
      f_typeNameFullyQualifiedJarStyle = typeNameFullyQualifiedJarStyle;
    }

    public Builder setWithin(Within value) {
      f_within = value;
      return this;
    }

    public Builder setRelativePath(String value) {
      f_relativePath = value;
      return this;
    }

    public Builder setTypeType(TypeType value) {
      f_typeType = value;
      return this;
    }

    public Builder setEclipseProjectName(String value) {
      f_eclipseProjectName = value;
      return this;
    }

    public Builder setLineNumber(int value) {
      f_lineNumber = value;
      return this;
    }

    public Builder setOffset(int value) {
      f_offset = value;
      return this;
    }

    public Builder setLenght(int value) {
      f_length = value;
      return this;
    }

    public Builder setJavaId(String value) {
      f_javaId = value;
      return this;
    }

    public Builder setEnclosingJavaId(String value) {
      f_enclosingJavaId = value;
      return this;
    }

    public IJavaRef build() {
      return new BasicJavaRef(f_within, f_typeNameFullyQualifiedJarStyle, f_relativePath, f_typeType, f_eclipseProjectName,
          f_lineNumber, f_offset, f_length, f_javaId, f_enclosingJavaId);
    }
  }

  @NonNull
  private final Within f_within;

  /**
   * Matches relative paths. We use {@link #f_relativePath} to generate many
   * results so we want to be sure it is correctly formatted.
   * <p>
   * Examples: <tt>some dir/java/lang/Object.class</tt>,
   * <tt>workspace/com/surelogic/JavaRef.java</tt>,
   * <tt>java.util.collections/Map$Entry</tt>, <tt>NotInAPkg.java</tt>
   */
  public static final String RELATIVE_PATH_REGEX = "([^/]*/)*[a-zA-Z]\\w*(\\.class|\\.java)";

  /**
   * The relative path of a .java or .class file. If the resource is within a
   * JAR this reference is {@code null} because
   * {@link #f_typeNameFullyQualifiedJarStyle} is used as the resources relative
   * path.
   * <p>
   * This string matches {@link #RELATIVE_PATH_REGEX} if non-{@code null}.
   */
  @Nullable
  private final String f_relativePath;

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
   * This string matches {@link #TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX}.
   */
  @NonNull
  private final String f_typeNameFullyQualifiedJarStyle;
  @NonNull
  private final TypeType f_typeType;
  @Nullable
  private final String f_eclipseProjectName;
  private final int f_lineNumber;
  private final int f_offset;
  private final int f_length;
  @Nullable
  private final String f_javaId;
  @Nullable
  private final String f_enclosingJavaId;

  protected BasicJavaRef(final @NonNull Within within, final @NonNull String typeNameFullyQualifiedJarStyle,
      final @Nullable String relativePathOrNullIfWithinJar, final @Nullable TypeType typeTypeOrNullifUnknown,
      final @Nullable String eclipseProjectNameOrNullIfUnknown, final int lineNumber, final int offset, final int length,
      final @Nullable String javaIdOrNull, final @Nullable String enclosingJavaIdOrNull) {
    if (within == null)
      throw new IllegalArgumentException(I18N.err(44, "within"));
    f_within = within;
    if (within == Within.JAR_FILE) {
      f_relativePath = null;
    } else {
      if (relativePathOrNullIfWithinJar == null)
        throw new IllegalArgumentException(I18N.err(254, "relativePathOrNullIfWithinJar"));
      if (!relativePathOrNullIfWithinJar.matches(RELATIVE_PATH_REGEX))
        throw new IllegalArgumentException(I18N.err(253, relativePathOrNullIfWithinJar, RELATIVE_PATH_REGEX));
      f_relativePath = relativePathOrNullIfWithinJar;
    }
    if (typeNameFullyQualifiedJarStyle == null)
      throw new IllegalArgumentException(I18N.err(44, "typeNameFullyQualifiedJarStyle"));
    if (!typeNameFullyQualifiedJarStyle.matches(TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX))
      throw new IllegalArgumentException(I18N.err(253, typeNameFullyQualifiedJarStyle, TYPE_NAME_FULLY_QUALIFIED_JAR_STYLE_REGEX));
    f_typeNameFullyQualifiedJarStyle = typeNameFullyQualifiedJarStyle;
    /*
     * .class or .java checks we can do if we have a non-null relative path
     */
    if (f_relativePath != null) {
      if (within == Within.JAVA_FILE) {
        if (!f_relativePath.endsWith("java"))
          throw new IllegalArgumentException(I18N.err(255, f_relativePath));
      } else {
        if (!f_relativePath.endsWith("class"))
          throw new IllegalArgumentException(I18N.err(256, f_relativePath));
      }
    }
    if (typeTypeOrNullifUnknown == null)
      f_typeType = TypeType.CLASS; // default
    else
      f_typeType = typeTypeOrNullifUnknown;
    f_eclipseProjectName = eclipseProjectNameOrNullIfUnknown;
    f_lineNumber = lineNumber > 0 ? lineNumber : -1;
    f_offset = offset > 0 ? offset : -1;
    f_length = length > 0 ? length : -1;
    f_javaId = javaIdOrNull;
    f_enclosingJavaId = enclosingJavaIdOrNull;
  }

  @NonNull
  public final Within getWithin() {
    return f_within;
  }

  public final boolean isFromSource() {
    return f_within == IJavaRef.Within.JAVA_FILE;
  }

  @NonNull
  public final String getRelativePath() {
    if (f_relativePath != null)
      return f_relativePath;
    else
      return f_typeNameFullyQualifiedJarStyle;
  }

  @NonNull
  public final String getFileName() {
    int index = f_relativePath.lastIndexOf('/');
    if (index == -1)
      return f_relativePath;
    else
      return f_relativePath.substring(index + 1);
  }

  public final int getLineNumber() {
    return f_lineNumber;
  }

  public final int getOffset() {
    return f_offset;
  }

  public final int getLength() {
    return f_length;
  }

  @NonNull
  public final String getEclipseProjectName() {
    if (f_eclipseProjectName == null)
      return SLUtility.UNKNOWN_PROJECT;
    else
      return f_eclipseProjectName;
  }

  @NonNull
  public final String getPackageName() {
    int index = f_typeNameFullyQualifiedJarStyle.indexOf('/');
    if (index < 1)
      return SLUtility.JAVA_DEFAULT_PACKAGE;
    else
      return f_typeNameFullyQualifiedJarStyle.substring(0, index);
  }

  @NonNull
  public final String getTypeName() {
    int index = f_typeNameFullyQualifiedJarStyle.indexOf('/');
    return f_typeNameFullyQualifiedJarStyle.substring(index + 1).replaceAll("\\$", ".");
  }

  @NonNull
  public final TypeType getTypeType() {
    return f_typeType;
  }

  @NonNull
  public final String getTypeNameFullyQualified() {
    return f_typeNameFullyQualifiedJarStyle.replaceAll("\\$|/", ".");
  }

  @NonNull
  public final String getTypeNameFullyQualifiedJarStyle() {
    return f_typeNameFullyQualifiedJarStyle;
  }

  @Nullable
  public final String getJavaId() {
    return f_javaId;
  }

  @Nullable
  public final String getEnclosingJavaId() {
    return f_enclosingJavaId;
  }
}
