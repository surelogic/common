package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

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
   * <td>{@link #setAbsolutePath(String)}</td>
   * <td>the absolute path to the file this code reference is within</td>
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
   * <tr>
   * <td>{@link #setJarRelativePath(String)}</td>
   * <td>the relative path within a <tt>.jar</tt> file that this code reference
   * is within</td>
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
   * <td>
   * {@link #setPositionRelativeToDeclaration(IJavaRef.Position)}</td>
   * <td>a code reference can be within or on a particular Java declaration</td>
   * <td>{@link Position#WITHIN}</td>
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
  public final static class Builder {

    private String f_absolutePath;
    private IDecl f_declaration;
    private String f_eclipseProjectName;
    private String f_enclosingJavaId;
    private String f_jarRelativePath;
    private String f_javaId;
    private int f_length = -1;
    private int f_lineNumber = -1;
    private int f_offset = -1;
    @NonNull
    private Position f_positionRelativeToDeclaration = Position.WITHIN;
    @NonNull
    private Within f_within = Within.JAVA_FILE;

    /**
     * Constructs a new builder that allows copy-then-modify from another code
     * location reference.
     * 
     * @param copy
     *          a code location reference.
     */
    public Builder(IJavaRef copy) {
      f_absolutePath = copy.getAbsolutePathOrNull();
      f_declaration = copy.getDeclaration();
      f_eclipseProjectName = copy.getEclipseProjectNameOrNull();
      f_enclosingJavaId = copy.getEnclosingJavaId();
      f_jarRelativePath = copy.getJarRelativePathOrNull();
      f_javaId = copy.getJavaId();
      f_length = copy.getLength();
      f_lineNumber = copy.getLineNumber();
      f_offset = copy.getOffset();
      f_positionRelativeToDeclaration = copy.getPositionRelativeToDeclaration();
      f_within = copy.getWithin();
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
     * Sets the the absolute path that this code reference is within, if known.
     * 
     * @param value
     *          an absolute path.
     * @return this builder.
     */
    public Builder setAbsolutePath(String value) {
      f_absolutePath = value;
      return this;
    }

    /**
     * Sets the Java declaration that this code reference is on or within.
     * 
     * @param value
     *          the Java declaration that this code reference is on or within.
     * @return this builder.
     */
    public Builder setDeclaration(IDecl value) {
      f_declaration = value;
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
     * Sets a declaration path used by viewers.
     * 
     * @param value
     *          a declaration path used by viewers.
     * @return this builder.
     */
    @Deprecated
    public Builder setEnclosingJavaId(String value) {
      f_enclosingJavaId = value;
      return this;
    }

    /**
     * Sets the path within the <tt>.jar</tt> file that this code reference is
     * within. if applicable.
     * 
     * @param value
     *          a path within a <tt>.jar</tt> file.
     * @return this builder.
     */
    public Builder setJarRelativePath(String value) {
      f_jarRelativePath = value;
      return this;
    }

    /**
     * Sets a declaration path used by viewers.
     * 
     * @param value
     *          a declaration path used by viewers.
     * @return this builder.
     */
    @Deprecated
    public Builder setJavaId(String value) {
      f_javaId = value;
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
     * Sets if this code reference is within or on a Java declaration.
     * 
     * @param value
     *          a position relative to the Java declaration. Ignored if
     *          {@code null}.
     * @return this builder.
     */
    public Builder setPositionRelativeToDeclaration(Position value) {
      if (value != null)
        f_positionRelativeToDeclaration = value;
      return this;
    }

    /**
     * Sets if this code reference is within a <tt>.java</tt> file, a
     * <tt>.class</tt> file, or a <tt>.jar</tt> file.
     * 
     * @param value
     *          what this code reference is within. Ignored if {@code null}.
     * @return this builder.
     */
    public Builder setWithin(Within value) {
      if (value != null)
        f_within = value;
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
      return new JavaRef(f_within, f_declaration, f_positionRelativeToDeclaration, f_eclipseProjectName, f_lineNumber, f_offset,
          f_length, f_absolutePath, f_jarRelativePath, f_javaId, f_enclosingJavaId);
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

  @Nullable
  private final String f_absolutePath;
  @NonNull
  private final IDecl f_declaration;
  @Nullable
  private final String f_eclipseProjectName;
  @Nullable
  private final String f_enclosingJavaId;
  @Nullable
  private final String f_jarRelativePath;
  @Nullable
  private final String f_javaId;
  /**
   * -1 indicates not valid.
   */
  private final int f_length;
  /**
   * -1 indicates not valid.
   */
  private final int f_lineNumber;
  /**
   * -1 indicates not valid.
   */
  private final int f_offset;
  @NonNull
  private final Position f_positionRelativeToDeclaration;
  @NonNull
  private final Within f_within;

  protected JavaRef(final @NonNull Within within, final @NonNull IDecl declaration,
      @NonNull Position positionRelativeToDeclaration, @Nullable final String eclipseProjectNameOrNull, final int lineNumber,
      final int offset, final int length, final @Nullable String absolutePathOrNull, final @Nullable String jarRelativePathOrNull,
      final @Nullable String javaIdOrNull, final @Nullable String enclosingJavaIdOrNull) {
    f_within = within;
    f_declaration = declaration;
    f_positionRelativeToDeclaration = positionRelativeToDeclaration;
    f_eclipseProjectName = eclipseProjectNameOrNull;
    f_lineNumber = lineNumber > 0 && lineNumber != Integer.MAX_VALUE ? lineNumber : -1;
    f_offset = offset > 0 && offset != Integer.MAX_VALUE ? offset : -1;
    f_length = length > 0 && length != Integer.MAX_VALUE ? length : -1;
    f_absolutePath = absolutePathOrNull;
    f_jarRelativePath = jarRelativePathOrNull;
    f_javaId = javaIdOrNull;
    f_enclosingJavaId = enclosingJavaIdOrNull;
  }

  @Nullable
  public String getAbsolutePathOrNull() {
    return f_absolutePath;
  }

  @NonNull
  public final IDecl getDeclaration() {
    return f_declaration;
  }

  @NonNull
  public final String getEclipseProjectName() {
    return f_eclipseProjectName == null ? SLUtility.UNKNOWN_PROJECT : f_eclipseProjectName;
  }

  @NonNull
  public final String getEclipseProjectNameOrEmpty() {
    return f_eclipseProjectName == null ? "" : f_eclipseProjectName;
  }

  @Nullable
  public final String getEclipseProjectNameOrNull() {
    return f_eclipseProjectName;
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

  @Nullable
  public String getJarRelativePathOrNull() {
    return f_jarRelativePath;
  }

  @Nullable
  public final String getJavaId() {
    return f_javaId;
  }

  public final int getLength() {
    return f_length;
  }

  public final int getLineNumber() {
    return f_lineNumber;
  }

  public final int getOffset() {
    return f_offset;
  }

  @NonNull
  public final String getPackageName() {
    return DeclUtil.getPackageName(f_declaration);
  }

  @NonNull
  public Position getPositionRelativeToDeclaration() {
    return f_positionRelativeToDeclaration;
  }

  @NonNull
  public final String getTypeNameFullyQualified() {
    return DeclUtil.getTypeNameFullyQualified(f_declaration);
  }

  @NonNull
  public final String getTypeNameOrNull() {
    return DeclUtil.getTypeNameOrNull(f_declaration);
  }

  @NonNull
  public final Within getWithin() {
    return f_within;
  }

  public final boolean isFromSource() {
    return f_within == IJavaRef.Within.JAVA_FILE;
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
    /*
     * Make sure this matches the getInstanceFrom() method below!
     * 
     * Also if this is output is changed in any way create a new version prefix.
     * Also note that the getInstanceFrom() method will need to support both the
     * old and new versions of the encoded string.
     */
    return ENCODE_V2 + f_within + "|" + f_positionRelativeToDeclaration + "|"
        + (f_eclipseProjectName == null ? "" : f_eclipseProjectName) + "|" + f_lineNumber + "|" + f_offset + "|" + f_length + "|"
        + (f_absolutePath == null ? "" : f_absolutePath) + "|" + (f_jarRelativePath == null ? "" : f_jarRelativePath) + "|"
        + Decl.encodeForPersistence(f_declaration);
  }

  public static final String ENCODE_V1 = "V1->";
  public static final String ENCODE_V2 = "V2->";

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
  public static IJavaRef parseEncodedForPersistence(@NonNull String encodedForPersistence) {
    if (encodedForPersistence == null)
      throw new IllegalArgumentException(I18N.err(44, "encodedForPersistence"));

    final int encodeVersion = getEncodeVersion(encodedForPersistence);

    if (encodeVersion != -1) {
      final StringBuilder b = new StringBuilder(encodedForPersistence.substring(ENCODE_V1.length()));
      final Within within = Within.valueOf(Decl.toNext("|", b));
      final Position positionRelativeToDeclaration = Position.valueOf(Decl.toNext("|", b));
      final String eclipseProjectName = Decl.toNext("|", b);
      final String lineNumberStr = Decl.toNext("|", b);
      final String offsetStr = Decl.toNext("|", b);
      final String lengthStr = Decl.toNext("|", b);
      final String absolutePath;
      final String jarRelativePath;
      if (encodeVersion == 2) {
        absolutePath = Decl.toNext("|", b);
        jarRelativePath = Decl.toNext("|", b);
      } else {
        absolutePath = jarRelativePath = "";
      }
      final IDecl declaration = Decl.parseEncodedForPersistence(b.toString());

      final Builder builder = new Builder(declaration);
      builder.setWithin(within);
      builder.setPositionRelativeToDeclaration(positionRelativeToDeclaration);
      if (!"".equals(eclipseProjectName))
        builder.setEclipseProjectName(eclipseProjectName);
      builder.setLineNumber(Integer.parseInt(lineNumberStr));
      builder.setOffset(Integer.parseInt(offsetStr));
      builder.setLength(Integer.parseInt(lengthStr));
      if (!"".equals(absolutePath))
        builder.setAbsolutePath(absolutePath);
      if (!"".equals(jarRelativePath))
        builder.setJarRelativePath(jarRelativePath);
      return builder.build();
    } else
      throw new IllegalArgumentException(I18N.err(270, encodedForPersistence));
  }

  /**
   * Gets the encoding version used in the passed string or -1 if the string
   * does not appear to be encoded properly.
   * 
   * @param encodedForPersistence
   *          a text string produced by {@link IJavaRef#encodeForPersistence()}.
   * @return the encoding version used in the passed string or -1 if the string
   *         does not appear to be encoded properly.
   */
  private static int getEncodeVersion(@NonNull String encodedForPersistence) {
    if (encodedForPersistence.startsWith(ENCODE_V1))
      return 1;
    else if (encodedForPersistence.startsWith(ENCODE_V2))
      return 2;
    return -1;
  }
}
