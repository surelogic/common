package com.surelogic.common;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.logging.SLLogger;

/**
 * Provides an implementation of this class that should be extended by other
 * implementations to get the details of the {@link IJavaRef} correct. Both the
 * {@link Builder} and the type should be extended.
 */
@Immutable
public class JavaRef implements IJavaRef {

  public static void main(String[] args) {
    IJavaRef r = new Builder("org.apache/Foo.Entry.A").setCUName("Bar.java").setEclipseProjectName("AntPrj").build();

    String s = r.encodeForPersistence();
    IJavaRef r1 = getInstanceFrom(s);

    System.out.println("r == r1 : " + (((JavaRef) r).f_encodedNames == ((JavaRef) r1).f_encodedNames));
    IJavaRef r2 = new Builder(r).setOffset(45).build();

    System.out.println("r :" + r);
    System.out.println("r2:" + r2);
    System.out.println("r == r2 : " + (((JavaRef) r).f_encodedNames == ((JavaRef) r2).f_encodedNames));

    System.out.println(((JavaRef) r).getEclipseProjectNameOrNullHelper());
    System.out.println(((JavaRef) r).getTypeNameFullyQualifiedSureLogicHelper());
    System.out.println(((JavaRef) r).getCUNameOrNullHelper());
    System.out.println(r.getSimpleFileName());
    System.out.println(r.getClasspathRelativePathname());
    System.out.println(r.toString());
    System.out.println(r.getClasspathRelativePathname());
    System.out.println(r.getTypeNameFullyQualifiedSureLogic());
    System.out.println(r.getPackageName());
    System.out.println(r.getPackageNameOrNull());
    System.out.println(r.getPackageNameSlash());
    System.out.println(r.getTypeName());
    System.out.println(r.getTypeNameDollarSign());
    System.out.println(r.getTypeNameFullyQualified());
    System.out.println(r.getEclipseProjectName());
    System.out.println(r.getEclipseProjectNameOrNull());
    System.out.println(r.getSimpleFileName());
    System.out.println(r.getClasspathRelativePathname());

    System.out.println(r.encodeForPersistence());
  }

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
   * from the type name.</td>
   * <td>{@code null}</td>
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
   * <td>{@link #setTypeType(IJavaRef.TypeType)}</td>
   * <td>the Java type this reference is within must be either a <tt>class</tt>,
   * an <tt>enum</tt>, or an <tt>interface</tt></td>
   * <td>{@link IJavaRef.TypeType#CLASS}</td>
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
    private String f_eclipseProjectName;
    protected String f_typeNameFullyQualifiedSureLogic;
    private String f_cuName;
    @NonNull
    protected TypeType f_typeType = TypeType.CLASS;
    protected int f_lineNumber;
    protected int f_offset;
    protected int f_length;
    protected String f_javaId;
    protected String f_enclosingJavaId;

    // to try to alias of encoded names (saves memory)
    private String f_copyEncodedNamesAlias;

    /**
     * Constructs a new builder that allows copy-then-modify from another code
     * location reference.
     * 
     * @param copy
     *          a code location reference.
     */
    public Builder(IJavaRef copy) {
      if (!(copy instanceof JavaRef))
        throw new IllegalArgumentException(I18N.err(261, copy.getClass().getName(), JavaRef.class.getName()));
      final JavaRef c = (JavaRef) copy;
      f_within = c.f_within;
      f_eclipseProjectName = c.getEclipseProjectNameOrNullHelper();
      f_typeNameFullyQualifiedSureLogic = c.getTypeNameFullyQualifiedSureLogicHelper();
      f_cuName = c.getCUNameOrNullHelper();
      f_typeType = c.f_typeType;
      f_lineNumber = c.f_lineNumber;
      f_offset = c.f_offset;
      f_length = c.f_length;
      f_javaId = c.f_javaId;
      f_enclosingJavaId = c.f_enclosingJavaId;

      f_copyEncodedNamesAlias = c.f_encodedNames;
    }

    /**
     * Constructs a new builder for a code reference within the passed type.
     * 
     * @param typeNameFullyQualifiedSureLogic
     *          a type name that is valid per
     *          {@link SLUtility#isValidTypeNameFullyQualifiedSureLogic(String)}
     */
    public Builder(@NonNull String typeNameFullyQualifiedSureLogic) {
      if (typeNameFullyQualifiedSureLogic == null)
        throw new IllegalArgumentException(I18N.err(44, "typeNameFullyQualifiedSureLogic"));
      f_typeNameFullyQualifiedSureLogic = typeNameFullyQualifiedSureLogic;
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
        f_cuName = value;
      } else {
        SLLogger.getLogger().warning(I18N.err(256, value));
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
        final StringBuilder b = new StringBuilder(f_typeNameFullyQualifiedSureLogic);
        int slashIndex = b.indexOf("/");
        b.replace(slashIndex + 1, b.length(), value);
        f_typeNameFullyQualifiedSureLogic = b.toString();
      } else {
        SLLogger.getLogger().warning(I18N.err(254, value));
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
      final StringBuilder b = new StringBuilder(f_typeNameFullyQualifiedSureLogic);
      int slashIndex = b.indexOf("/");
      if (value == null)
        value = "";
      if ("".equals(value)) {
        b.delete(0, slashIndex);
      } else if (SLUtility.isValidDotSeparatedJavaIdentifier(value)) {
        b.replace(0, slashIndex, value);
      } else {
        SLLogger.getLogger().warning(I18N.err(255, value));
        return this;
      }
      f_typeNameFullyQualifiedSureLogic = b.toString();
      return this;
    }

    /**
     * Sets the type of Java type this reference is within. It must be either a
     * <tt>class</tt>, an <tt>enum</tt>, or an <tt>interface</tt>.
     * 
     * @param value
     *          the type of Java type this reference is within.
     * @return this builder.
     */
    public Builder setTypeType(TypeType value) {
      if (value != null)
        f_typeType = value;
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

    protected final String getEncodedNames() {
      final StringBuilder b = new StringBuilder();
      if (f_eclipseProjectName != null)
        b.append(f_eclipseProjectName);
      b.append(':');
      b.append(f_typeNameFullyQualifiedSureLogic);
      b.append('|');
      // only use the source CU name if it is different than the type name
      if (f_cuName != null && f_within == Within.JAVA_FILE && cuNameReallyDifferent())
        b.append(f_cuName);
      // try to alias from the reference we copied, if possible
      final String result = b.toString();
      if (result.equals(f_copyEncodedNamesAlias))
        return f_copyEncodedNamesAlias;
      else
        return result;
    }

    private boolean cuNameReallyDifferent() {
      final StringBuilder b = new StringBuilder(f_typeNameFullyQualifiedSureLogic);
      int slashIndex = b.indexOf("/");
      b.delete(0, slashIndex + 1);
      int dotIndex = b.indexOf(".");
      if (dotIndex != -1)
        b.delete(dotIndex, b.length());
      return !f_cuName.equals(b.toString());
    }

    /**
     * Strict builder&mdash;throws an exception if it fails.
     * 
     * @return a code reference.
     */
    public IJavaRef build() {
      if (!SLUtility.isValidTypeNameFullyQualifiedSureLogic(f_typeNameFullyQualifiedSureLogic))
        throw new IllegalArgumentException(I18N.err(253, f_typeNameFullyQualifiedSureLogic));

      return new JavaRef(f_within, getEncodedNames(), f_typeType, f_lineNumber, f_offset, f_length, f_javaId, f_enclosingJavaId);
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

  /**
   * This encoded name string is formatted as follows:
   * <p>
   * <i>project-name</i><tt>:</tt><i>type-name</i><tt>|</tt><i>CU-name</i>
   * <p>
   * <b>Project name</b>: This is the Eclipse project name if known.
   * <p>
   * <b>Type name</b> The type name is mandatory and must be valid according to
   * {@link SLUtility#isValidTypeNameFullyQualifiedSureLogic(String)}.
   * <p>
   * <b>Compilation Unit (.java file) name:</b> In the rare case the type name
   * and the CU name don't match because more than one top-level type declared
   * in a CU. The encoded string assumes <tt>.java</tt> as the file suffix and
   * does not include it. Note that the CU name can only be different from the
   * type name if and only if {@link #getWithin()} == {@link Within#JAVA_FILE}.
   * <p>
   * For example a value of <tt>":org.apache/Foo|Bar"</tt> would indicate the
   * top-level type <tt>Foo</tt> is declared in the CU <tt>Bar.java</tt> in the
   * package <tt>org.apache</tt>. The Eclipse project is unknown.
   * 
   * @see IJavaRef#getTypeNameFullyQualifiedSureLogic()
   */
  @NonNull
  private final String f_encodedNames;

  /**
   * This is for testing {@link Builder} correctly aliases encoded names.
   * 
   * @return the encoded names of this.
   */
  @NonNull
  public String getEncodedNames() {
    return f_encodedNames;
  }

  @Nullable
  private String getEclipseProjectNameOrNullHelper() {
    final int colonIndex = f_encodedNames.indexOf(':');
    if (colonIndex < 1)
      return null;
    else
      return f_encodedNames.substring(0, colonIndex);
  }

  private String getTypeNameFullyQualifiedSureLogicHelper() {
    final int colonIndex = f_encodedNames.indexOf(':');
    final int barIndex = f_encodedNames.indexOf('|');
    return f_encodedNames.substring(colonIndex + 1, barIndex);
  }

  private String getCUNameOrNullHelper() {
    final int barIndex = f_encodedNames.indexOf('|');
    if (f_encodedNames.length() > barIndex + 1)
      return f_encodedNames.substring(barIndex + 1);
    else
      return null;
  }

  @NonNull
  private final TypeType f_typeType;
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

  protected JavaRef(final @NonNull Within within, final @NonNull String encodedNames, final @NonNull TypeType typeType,
      final int lineNumber, final int offset, final int length, final @Nullable String javaIdOrNull,
      final @Nullable String enclosingJavaIdOrNull) {
    f_within = within;
    f_encodedNames = encodedNames;
    f_typeType = typeType;
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
  public final String getEclipseProjectNameOrNull() {
    final String name = getEclipseProjectNameOrNullHelper();
    if (name == null)
      return null;
    else
      return name;
  }

  @NonNull
  public final String getPackageName() {
    final String name = getPackageNameOrNull();
    return name == null ? SLUtility.JAVA_DEFAULT_PACKAGE : name;
  }

  @Nullable
  public final String getPackageNameOrNull() {
    final String name = getTypeNameFullyQualifiedSureLogicHelper();
    int slashIndex = name.indexOf('/');
    if (slashIndex < 1)
      return null;
    else
      return name.substring(0, slashIndex);
  }

  @NonNull
  public final String getPackageNameSlash() {
    final String name = getPackageNameOrNull();
    return name == null ? "" : name.replaceAll("\\.", "/");
  }

  @NonNull
  public final String getTypeName() {
    final String name = getTypeNameFullyQualifiedSureLogicHelper();
    int slashIndex = name.indexOf('/');
    return name.substring(slashIndex + 1);
  }

  @NonNull
  public final String getTypeNameDollarSign() {
    String name = getTypeName();
    return name.replaceAll("\\.", "\\$");
  }

  @NonNull
  public final TypeType getTypeType() {
    return f_typeType;
  }

  @NonNull
  public final String getTypeNameFullyQualified() {
    final String name = getTypeNameFullyQualifiedSureLogicHelper();
    return name.replaceAll("/", ".");
  }

  @NonNull
  public final String getTypeNameFullyQualifiedSureLogic() {
    return getTypeNameFullyQualifiedSureLogicHelper();
  }

  @NonNull
  public final String getSimpleFileName() {
    final StringBuilder b = new StringBuilder(getTypeName());
    if (getWithin() == Within.JAVA_FILE) {
      final String name = getCUNameOrNullHelper();
      if (name == null) {
        /*
         * The nested type is inside the .java file of the outermost type, if
         * any nesting.
         */
        int dollarIndex = b.indexOf(".");
        if (dollarIndex != -1) {
          b.delete(dollarIndex, b.length());
        }
      } else {
        /*
         * The type must is declared at the top-level in a different file.
         */
        b.setLength(0); // clear
        b.append(name);
      }
      b.append(".java");
    } else {
      b.append(".class");
    }
    return b.toString();
  }

  @NonNull
  public final String getClasspathRelativePathname() {
    final StringBuilder b = new StringBuilder(getPackageNameSlash());
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
    if (f_lineNumber != -1)
      return Long.valueOf(f_encodedNames.hashCode() + f_lineNumber);
    else
      return Long.valueOf(f_encodedNames.hashCode());
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder("JavaRef(");
    b.append(f_encodedNames);
    b.append(",within=").append(f_within);
    b.append(",typetype=").append(f_typeType);
    b.append(",line=").append(f_lineNumber);
    b.append(",offset=").append(f_offset);
    b.append(",length=").append(f_length);
    b.append(")");
    return b.toString();
  }

  public final String encodeForPersistence() {
    /*
     * Make sure this matches the getInstanceFrom() method below!
     * 
     * Also if this is output is changed in any way create a new version prefix.
     * Also note that the getInstanceFrom() method will need to support both the
     * old and new versions of the encoded string.
     */
    return ENCODE_V1 + f_encodedNames + "|" + f_within + "|" + f_typeType + "|" + f_lineNumber + "|" + f_offset + "|" + f_length
        + "|" + (f_javaId == null ? "" : f_javaId) + "|" + (f_enclosingJavaId == null ? "" : f_javaId) + "|";
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
    if (encodedForPersistence == null)
      throw new IllegalArgumentException(I18N.err(44, "encodedForPersistence"));
    if (encodedForPersistence.startsWith(ENCODE_V1)) {
      final StringBuilder b = new StringBuilder(encodedForPersistence.substring(ENCODE_V1.length()));
      final String eclipseProjectName = toNext(":", b);
      final String typeNameFullyQualifiedSureLogic = toNext("|", b);
      final String cuName = toNext("|", b);
      final String withinStr = toNext("|", b);
      final String typeTypeStr = toNext("|", b);
      final String lineNumberStr = toNext("|", b);
      final String offsetStr = toNext("|", b);
      final String lengthStr = toNext("|", b);
      final String javaId = toNext("|", b);
      final String enclosingJavaId = toNext("|", b);

      final Builder builder = new Builder(typeNameFullyQualifiedSureLogic);
      builder.setWithin(Within.valueOf(withinStr));
      builder.setTypeType(TypeType.valueOf(typeTypeStr));
      if (!"".equals(eclipseProjectName))
        builder.setEclipseProjectName(eclipseProjectName);
      if (!"".equals(cuName))
        builder.setCUName(cuName);
      builder.setLineNumber(Integer.parseInt(lineNumberStr));
      builder.setOffset(Integer.parseInt(offsetStr));
      builder.setLength(Integer.parseInt(lengthStr));
      if (!"".equals(javaId))
        builder.setJavaId(javaId);
      if (!"".equals(enclosingJavaId))
        builder.setEnclosingJavaId(enclosingJavaId);
      return builder.build();
    } else
      throw new IllegalArgumentException(I18N.err(260, encodedForPersistence));
  }

  private static String toNext(final String str, final StringBuilder b) {
    final int barIndex = b.indexOf(str);
    final String result = b.substring(0, barIndex);
    b.delete(0, barIndex + 1);
    return result;
  }
}
