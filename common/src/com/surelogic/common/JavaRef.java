package com.surelogic.common;

import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

/**
 * Provides an implementation of this class that should be extended by other
 * implementations to get the details of the {@link IJavaRef} correct. Both the
 * {@link Builder} and the type should be extended.
 */
@ThreadSafe
public class JavaRef implements IJavaRef {

  @NotThreadSafe
  public static class Builder {

    protected Within f_within = Within.JAVA_FILE;
    protected final String f_typeNameFullyQualifiedSureLogic;
    protected TypeType f_typeType = TypeType.CLASS;
    protected String f_eclipseProjectName;
    protected int f_lineNumber;
    protected int f_offset;
    protected int f_length;
    protected String f_javaId;
    protected String f_enclosingJavaId;

    public Builder(IJavaRef copy) {
      f_within = copy.getWithin();
      f_typeNameFullyQualifiedSureLogic = copy.getTypeNameFullyQualifiedSureLogic();
      f_typeType = copy.getTypeType();
      f_eclipseProjectName = copy.getEclipseProjectName();
      f_lineNumber = copy.getLineNumber();
      f_offset = copy.getOffset();
      f_length = copy.getLength();
      f_javaId = copy.getJavaId();
      f_enclosingJavaId = copy.getEnclosingJavaId();
    }

    public Builder(@NonNull String typeNameFullyQualifiedSureLogic) {
      f_typeNameFullyQualifiedSureLogic = typeNameFullyQualifiedSureLogic;
    }

    public Builder setWithin(Within value) {
      f_within = value;
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

    public Builder setLength(int value) {
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
      return new JavaRef(f_within, f_typeNameFullyQualifiedSureLogic, f_typeType, f_eclipseProjectName, f_lineNumber, f_offset,
          f_length, f_javaId, f_enclosingJavaId);
    }

    public IJavaRef buildOrNullOnFailure() {
      try {
        return build();
      } catch (Exception ignore) {
        // ignore
      }
      return null;
    }
  }

  @NonNull
  private final Within f_within;

  /**
   * This string is valid according to
   * {@link SLUtility#isValidTypeNameFullyQualifiedSureLogic(String)}.
   * 
   * @see IJavaRef#getTypeNameFullyQualifiedSureLogic()
   */
  @NonNull
  private final String f_typeNameFullyQualifiedSureLogic;
  @NonNull
  private final TypeType f_typeType;
  @Nullable
  private final String f_eclipseProjectName;
  /**
   * -1 indicates not valid.
   */
  private final int f_lineNumber;
  /**
   * -1 indicates not valid.
   */
  private final int f_offset;
  /**
   * -1 indicates not valid.
   */
  private final int f_length;
  @Nullable
  private final String f_javaId;
  @Nullable
  private final String f_enclosingJavaId;

  protected JavaRef(final @NonNull Within within, final @NonNull String typeNameFullyQualifiedSureLogic,
      final @Nullable TypeType typeTypeOrNullifUnknown, final @Nullable String eclipseProjectNameOrNullIfUnknown,
      final int lineNumber, final int offset, final int length, final @Nullable String javaIdOrNull,
      final @Nullable String enclosingJavaIdOrNull) {
    if (within == null)
      throw new IllegalArgumentException(I18N.err(44, "within"));
    f_within = within;
    if (typeNameFullyQualifiedSureLogic == null)
      throw new IllegalArgumentException(I18N.err(44, "typeNameFullyQualifiedSureLogic"));
    if (!SLUtility.isValidTypeNameFullyQualifiedSureLogic(typeNameFullyQualifiedSureLogic))
      throw new IllegalArgumentException(I18N.err(253, typeNameFullyQualifiedSureLogic));
    f_typeNameFullyQualifiedSureLogic = typeNameFullyQualifiedSureLogic;
    if (typeTypeOrNullifUnknown == null)
      f_typeType = TypeType.CLASS; // default
    else
      f_typeType = typeTypeOrNullifUnknown;
    f_eclipseProjectName = eclipseProjectNameOrNullIfUnknown;
    f_lineNumber = lineNumber > 0 && lineNumber != Integer.MAX_VALUE ? lineNumber : -1;
    f_offset = offset > 0 && offset != Integer.MAX_VALUE ? offset : -1;
    f_length = length > 0 && length != Integer.MAX_VALUE ? length : -1;
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
    final String name = getEclipseProjectNameOrNull();
    return name == null ? SLUtility.UNKNOWN_PROJECT : name;
  }

  @Nullable
  public String getEclipseProjectNameOrNull() {
    if (f_eclipseProjectName == null)
      return null;
    else
      return f_eclipseProjectName;
  }

  @NonNull
  public final String getPackageName() {
    final String name = getPackageNameOrNull();
    return name == null ? SLUtility.JAVA_DEFAULT_PACKAGE : name;
  }

  @Nullable
  public String getPackageNameOrNull() {
    int slashIndex = f_typeNameFullyQualifiedSureLogic.indexOf('/');
    if (slashIndex < 1)
      return null;
    else
      return f_typeNameFullyQualifiedSureLogic.substring(0, slashIndex);
  }

  @NonNull
  public String getPackageNameSlash() {
    final String packageName = getPackageName();
    return packageName.replaceAll("\\.", "/");
  }

  @NonNull
  public final String getTypeName() {
    int slashIndex = f_typeNameFullyQualifiedSureLogic.indexOf('/');
    return f_typeNameFullyQualifiedSureLogic.substring(slashIndex + 1);
  }

  @NonNull
  public String getTypeNameDollarSign() {
    final String name = getTypeName();
    return name.replaceAll("\\.", "$");
  }

  @NonNull
  public final TypeType getTypeType() {
    return f_typeType;
  }

  @NonNull
  public final String getTypeNameFullyQualified() {
    return f_typeNameFullyQualifiedSureLogic.replaceAll("/", ".");
  }

  @NonNull
  public final String getTypeNameFullyQualifiedSureLogic() {
    return f_typeNameFullyQualifiedSureLogic;
  }

  @Nullable
  public final String getJavaId() {
    return f_javaId;
  }

  @Nullable
  public final String getEnclosingJavaId() {
    return f_enclosingJavaId;
  }

  public Long getHash() {
    if (f_lineNumber != -1)
      return Long.valueOf(f_typeNameFullyQualifiedSureLogic.hashCode() + f_lineNumber);
    else
      return Long.valueOf(f_typeNameFullyQualifiedSureLogic.hashCode());
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder("JavaRef(");
    b.append(f_typeNameFullyQualifiedSureLogic);
    b.append(",within=").append(f_within);
    b.append(",typetype=").append(f_typeType);
    b.append(",line=").append(f_lineNumber);
    b.append(",offset=").append(f_offset);
    b.append(",length=").append(f_length);
    b.append(")");
    return b.toString();
  }
}
