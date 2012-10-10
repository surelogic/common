package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * Provides an implementation of this class that should be extended by other
 * implementations to get the details of the {@link IJavaRef} correct. Both the
 * {@link Builder} and the type should be extended.
 */
@Immutable
public class JavaRef implements IJavaRef {

  /**
   * Builder for {@link IJavaRef} instances. Copy-and-modify is supported via
   * {@link Builder#Builder(IJavaRef)}.
   * <p>
   * The default values are listed in the table below.
   * <p>
   * <table border=1>
   * <tr>
   * <th>Method</th>
   * <th>Description</th>
   * <th>Default</th>
   * </tr>
   * <tr>
   * <td>{@link #setCUName(String)}</td>
   * <td><tt>.java</tt> file name in the very rare case that it is different
   * from the type name</td>
   * <td>{@code null}</td>
   * </tr>
   * <tr>
   * <td>{@link #setDeclaration(IDecl)}</td>
   * <td>the Java declaration that this code reference is on or within</td>
   * <td>none</td>
   * </tr>
   * <tr>
   * <td>{@link #setEclipseProjectName(String)}</td>
   * <td>the Eclipse project name the code reference is within</td>
   * <td>{@code null}</td>
   * </tr>
   * <tr>
   * <td>{@link #setEnclosingJavaId(String)}</td>
   * <td>a declaration path used by viewers</td>
   * <td>{@code null}</td>
   * </tr>
   * <tr>
   * <td>{@link #setIsOnDeclaration(boolean)}</td>
   * <td>flags if this code reference is on (true) or within (false) the
   * declaration returned by {@link IJavaRef#getDeclaration()}</td>
   * <td>{@code false} (within the declaration)</td>
   * </tr>
   * <tr>
   * <td>{@link #setJavaId(String)}</td>
   * <td>a declaration path used by viewers</td>
   * <td>{@code null}</td>
   * </tr>
   * <tr>
   * <td>{@link #setLength(int)}</td>
   * <td>the character length of the code reference in the source file</td>
   * <td>-1</td>
   * </tr>
   * <tr>
   * <td>{@link #setLineNumber(int)}</td>
   * <td>the line number of the code reference in the source file</td>
   * <td>-1</td>
   * </tr>
   * <tr>
   * <td>{@link #setOffset(int)}</td>
   * <td>the character offset of the code reference in the source file</td>
   * <td>-1</td>
   * </tr>
   * <tr>
   * <td>{@link #setPackageName(String)}</td>
   * <td>the package name this reference is within</td>
   * <td>the name given to construct this builder</td>
   * </tr>
   * <tr>
   * <td>{@link #setTypeName(String)}</td>
   * <td>the simple type name this reference is within, including nested
   * types&mdash;just not the package name</td>
   * <td>the name given to construct this builder</td>
   * </tr>
   * <tr>
   * <td>{@link #setWithin(IJavaRef.Within)}</td>
   * <td>a code reference can be within a <tt>.java</tt> file, a <tt>.class</tt>
   * file, or a <tt>.jar</tt> file</td>
   * <td>{@link IJavaRef.Within#JAVA_FILE}</td>
   * </tr>
   * </table>
   */
  @NotThreadSafe
  public static class Builder {

    @NonNull
    protected Within f_within = Within.JAVA_FILE;
    protected String f_eclipseProjectName;
    protected IDecl f_declaration;
    protected boolean f_isOnDeclaration = false;
    protected int f_lineNumber;
    protected int f_offset;
    protected int f_length;
    protected String f_javaId;
    protected String f_enclosingJavaId;

    /**
     * Constructs a new builder that allows copy-then-modify from another code
     * location reference.
     * 
     * @param copy
     *          a code location reference.
     */
    public Builder(IJavaRef copy) {
      f_within = copy.getWithin();
      f_eclipseProjectName = copy.getEclipseProjectNameOrNull();
      f_declaration = copy.getDeclaration();
      f_lineNumber = copy.getLineNumber();
      f_offset = copy.getOffset();
      f_length = copy.getLength();
      f_javaId = copy.getJavaId();
      f_enclosingJavaId = copy.getEnclosingJavaId();
    }

    /**
     * Constructs a new builder for a code reference within the passed type.
     * 
     * @param declaration
     *          a Java declaration that this code reference is on or within.
     */
    public Builder(@NonNull IDecl declaration) {
      if (declaration == null)
        throw new IllegalArgumentException(I18N.err(44, "declaration"));
      f_declaration = declaration;
    }

    /**
     * Sets if this code reference is within a <tt>.java</tt> file, a
     * <tt>.class</tt> file, or a <tt>.jar</tt> file.
     * 
     * @param value
     *          what this code reference is within.
     * @return this builder.
     */
    public Builder setWithin(Within value) {
      if (value != null)
        f_within = value;
      return this;
    }

    /**
     * This method is used to explicitly set the name of the <tt>.java</tt>
     * file&mdash;the compilation unit&mdash; when the file name is not based
     * upon the type name this code refers to. This can occur when more then two
     * top-level types are declared in the same compilation unit.
     * <p>
     * This setting only makes sense if we are within <tt>.java</tt> file (
     * {@link Within#JAVA_FILE}) because that is the only time the CU name can
     * be different from the type name. If it is set on a binary, the setting is
     * ignored.
     * 
     * @param value
     *          the name of the file. <tt>.java</tt> may suffix the name or
     *          not&mdash;if not it will be added automatically. The name is
     *          ignored if the simple file name (no extension) is not a valid
     *          Java type name. A warning is logged if the value is ignored.
     * @return this builder.
     */
    public Builder setCUName(String value) {
      if (value == null)
        return this;
      final int suffix = value.indexOf(".java");
      if (suffix != -1)
        value = value.substring(0, suffix);
      if (SLUtility.isValidJavaIdentifier(value)) {
        // TODO f_cuName = value;
      } else {
        SLLogger.getLogger().warning(I18N.err(266, value));
      }
      return this;
    }

    /**
     * Changes the type name this code reference refers to.
     * <p>
     * For example, if a builder, <tt>b</tt>, contains the full name
     * <tt>"java.util/Map.Entry"</tt> and <tt>b.setTypeName("List")</tt> is
     * invoked the full name will become <tt>"java.util/List"</tt>.
     * 
     * @param value
     *          the new type name, ignored if not a valid Java type name. A
     *          warning is logged if the value is ignored.
     * @return this builder.
     */
    public Builder setTypeName(String value) {
      if (SLUtility.isValidDotSeparatedJavaIdentifier(value)) {
        // TODO the whole IDecl needs to be modified
      } else {
        SLLogger.getLogger().warning(I18N.err(264, value));
      }
      return this;
    }

    /**
     * Changes the package type name this code reference refers to.
     * <p>
     * For example, if a builder, <tt>b</tt>, contains the full name
     * <tt>"java.util/Map.Entry"</tt> and
     * <tt>b.setPackageName("org.apache.collections")</tt> is invoked the full
     * name will become <tt>"org.apache.collections/Map.Entry"</tt>.
     * 
     * @param value
     *          the new package name, ignored if not a valid Java type name. A
     *          warning is logged if the value is ignored. Passing {@code null}
     *          or <tt>""</tt> changes the package to the default package.
     * @return this builder.
     */
    public Builder setPackageName(String value) {
      if (SLUtility.isValidDotSeparatedJavaIdentifier(value)) {
        // TODO the whole IDecl needs to be modified
      } else {
        SLLogger.getLogger().warning(I18N.err(265, value));
      }
      return this;
    }

    /**
     * Sets the Eclipse project name the code reference is within.
     * 
     * @param value
     *          the Eclipse project name the code reference is within.
     * @return this builder.
     */
    public Builder setEclipseProjectName(String value) {
      f_eclipseProjectName = value;
      return this;
    }

    public Builder setDeclaration(IDecl value) {
      f_declaration = value;
      return this;
    }

    public Builder setIsOnDeclaration(boolean value) {
      f_isOnDeclaration = value;
      return this;
    }

    /**
     * Sets the line number of the code reference in the source file.
     * 
     * @param value
     *          the line number of the code reference in the source file
     * @return this builder.
     */
    public Builder setLineNumber(int value) {
      f_lineNumber = value;
      return this;
    }

    /**
     * Sets the character offset of the code reference in the source file.
     * 
     * @param value
     *          the character offset of the code reference in the source file.
     * @return this builder.
     */
    public Builder setOffset(int value) {
      f_offset = value;
      return this;
    }

    /**
     * Sets the character length of the code reference in the source file.
     * 
     * @param value
     *          the character length of the code reference in the source file.
     * @return this builder.
     */
    public Builder setLength(int value) {
      f_length = value;
      return this;
    }

    /**
     * Sets a declaration path used by viewers.
     * 
     * @param value
     *          a declaration path used by viewers.
     * @return this builder.
     */
    public Builder setJavaId(String value) {
      f_javaId = value;
      return this;
    }

    /**
     * Sets a declaration path used by viewers.
     * 
     * @param value
     *          a declaration path used by viewers.
     * @return this builder.
     */
    public Builder setEnclosingJavaId(String value) {
      f_enclosingJavaId = value;
      return this;
    }

    /**
     * Strict builder&mdash;throws an exception if it fails.
     * 
     * @return a code reference.
     */
    public IJavaRef build() {
      if (f_declaration == null)
        throw new IllegalArgumentException(I18N.err(44, "declaration"));
      return new JavaRef(f_within, f_declaration, f_isOnDeclaration, f_eclipseProjectName, f_lineNumber, f_offset, f_length,
          f_javaId, f_enclosingJavaId);
    }

    /**
     * Sloppy builder&mdash;returns {@code null} if it fails
     * 
     * @return a code reference or {@code null}.
     */
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

  @NonNull
  private final IDecl f_declaration;
  private final boolean f_isOnDeclaration;

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

  protected JavaRef(final @NonNull Within within, final @NonNull IDecl declaration, final boolean isOnDeclaration,
      @Nullable final String eclipseProjectNameOrNull, final int lineNumber, final int offset, final int length,
      final @Nullable String javaIdOrNull, final @Nullable String enclosingJavaIdOrNull) {
    f_within = within;
    f_declaration = declaration;
    f_isOnDeclaration = isOnDeclaration;
    f_eclipseProjectName = eclipseProjectNameOrNull;
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
    return f_eclipseProjectName == null ? SLUtility.UNKNOWN_PROJECT : f_eclipseProjectName;
  }

  @Nullable
  public final String getEclipseProjectNameOrNull() {
    return f_eclipseProjectName;
  }

  @NonNull
  public IDecl getDeclaration() {
    return f_declaration;
  }

  public boolean isOnDeclaration() {
    return f_isOnDeclaration;
  }

  @NonNull
  public final String getPackageName() {
    return DeclUtil.getPackageName(f_declaration);
  }

  @NonNull
  public final String getTypeNameOrNull() {
    return DeclUtil.getTypeNameOrNull(f_declaration);
  }

  @NonNull
  public final String getTypeNameFullyQualified() {
    return DeclUtil.getTypeNameFullyQualified(f_declaration);
  }

  @NonNull
  public final String getSimpleFileName() {
    final StringBuilder b = new StringBuilder();
    final String typeNameDollar = DeclUtil.getTypeNameDollarSignOrNull(f_declaration);
    if (typeNameDollar == null) {
      b.append(SLUtility.PACKAGE_INFO);
    } else {
      b.append(typeNameDollar);
      if (f_within == Within.JAVA_FILE) {
        /*
         * The nested type is inside the .java file of the outermost type, if
         * any nesting.
         */
        int dollarIndex = b.indexOf("$");
        if (dollarIndex != -1) {
          b.delete(dollarIndex, b.length());
        }
      }
    }
    if (f_within == Within.JAVA_FILE)
      b.append(".java");
    else
      b.append(".class");
    return b.toString();
  }

  @NonNull
  public final String getClasspathRelativePathname() {
    final StringBuilder b = new StringBuilder(DeclUtil.getPackageNameSlash(f_declaration));
    b.append('/');
    b.append(getSimpleFileName());
    return b.toString();
  }

  @Nullable
  public final String getJavaId() {
    return f_javaId;
  }

  @Nullable
  public final String getEnclosingJavaId() {
    return f_enclosingJavaId;
  }

  public final Long getHash() {
    String encodedNames = getEclipseProjectName() + DeclUtil.getTypeNameFullyQualifiedSureLogic(f_declaration);
    if (f_lineNumber != -1)
      return Long.valueOf(encodedNames.hashCode() + f_lineNumber);
    else
      return Long.valueOf(encodedNames.hashCode());
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder("JavaRef(");
    if (f_eclipseProjectName != null) {
      b.append(f_eclipseProjectName);
      b.append(":");
    }
    b.append(DeclUtil.getTypeNameFullyQualifiedSureLogic(f_declaration));
    b.append(",within=").append(f_within);
    b.append(",kind=").append(DeclUtil.getTypeKind(f_declaration));
    b.append(",line=").append(f_lineNumber);
    b.append(",offset=").append(f_offset);
    b.append(",length=").append(f_length);
    b.append(")");
    return b.toString();
  }

  public final String encodeForPersistence() {
    // /*
    // * Make sure this matches the getInstanceFrom() method below!
    // *
    // * Also if this is output is changed in any way create a new version
    // prefix.
    // * Also note that the getInstanceFrom() method will need to support both
    // the
    // * old and new versions of the encoded string.
    // */
    // return ENCODE_V1 + f_encodedNames + "|" + f_within + "|" + f_typeType +
    // "|" + f_lineNumber + "|" + f_offset + "|" + f_length
    // + "|" + (f_javaId == null ? "" : f_javaId) + "|" + (f_enclosingJavaId ==
    // null ? "" : f_javaId) + "|";
    return null; // TODO
  }

  public static final String ENCODE_V1 = "V1->";

  /**
   * Constructs a code reference from a text string produced by
   * {@link IJavaRef#encodeForPersistence()}. This method will fail if the
   * string format is invalid, or is in an unsupported version&mdash;some sort
   * of exception will be thrown.
   * 
   * @param encodedForPersistence
   *          a text string produced by {@link IJavaRef#encodeForPersistence()}.
   * @return a code reference.
   * @see IJavaRef#encodeForPersistence()
   */
  @NonNull
  public static IJavaRef getInstanceFrom(@NonNull String encodedForPersistence) {
    return null; // TODO
    // if (encodedForPersistence == null)
    // throw new IllegalArgumentException(I18N.err(44,
    // "encodedForPersistence"));
    // if (encodedForPersistence.startsWith(ENCODE_V1)) {
    // final StringBuilder b = new
    // StringBuilder(encodedForPersistence.substring(ENCODE_V1.length()));
    // final String eclipseProjectName = toNext(":", b);
    // final String typeNameFullyQualifiedSureLogic = toNext("|", b);
    // final String cuName = toNext("|", b);
    // final String withinStr = toNext("|", b);
    // final String typeTypeStr = toNext("|", b);
    // final String lineNumberStr = toNext("|", b);
    // final String offsetStr = toNext("|", b);
    // final String lengthStr = toNext("|", b);
    // final String javaId = toNext("|", b);
    // final String enclosingJavaId = toNext("|", b);
    //
    // final Builder builder = new Builder(typeNameFullyQualifiedSureLogic);
    // builder.setWithin(Within.valueOf(withinStr));
    // builder.setTypeType(TypeType.valueOf(typeTypeStr));
    // if (!"".equals(eclipseProjectName))
    // builder.setEclipseProjectName(eclipseProjectName);
    // if (!"".equals(cuName))
    // builder.setCUName(cuName);
    // builder.setLineNumber(Integer.parseInt(lineNumberStr));
    // builder.setOffset(Integer.parseInt(offsetStr));
    // builder.setLength(Integer.parseInt(lengthStr));
    // if (!"".equals(javaId))
    // builder.setJavaId(javaId);
    // if (!"".equals(enclosingJavaId))
    // builder.setEnclosingJavaId(enclosingJavaId);
    // return builder.build();
    // } else
    // throw new IllegalArgumentException(I18N.err(270, encodedForPersistence));
  }

  private static String toNext(final String str, final StringBuilder b) {
    final int barIndex = b.indexOf(str);
    final String result = b.substring(0, barIndex);
    b.delete(0, barIndex + 1);
    return result;
  }
}
