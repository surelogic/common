package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

/**
 * Provides an implementation of this class that should be extended by other
 * implementations to get the details of the {@link IJavaRef} correct. Both the
 * {@link Builder} and the type should be extended.
 */
@Immutable
@ValueObject
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
   * <tr>
   * <td>{@link #setJarRelativePath(String)}</td>
   * <td>the relative path within a <tt>.jar</tt> file that this code reference
   * is within</td>
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
   * <td>{@link Position#WITHIN_DECL}</td>
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
    private String f_jarRelativePath;
    private int f_length = -1;
    private int f_lineNumber = -1;
    private int f_offset = -1;
    @NonNull
    private Position f_positionRelativeToDeclaration = Position.WITHIN_DECL;
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
      f_jarRelativePath = copy.getJarRelativePathOrNull();
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
      if (value != null && value.startsWith(SLUtility.LIBRARY_PROJECT))
        f_eclipseProjectName = SLUtility.LIBRARY_PROJECT;
      else
        f_eclipseProjectName = value;
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
          f_length, f_absolutePath, f_jarRelativePath);
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
  private final String f_jarRelativePath;
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
      final int offset, final int length, final @Nullable String absolutePathOrNull, final @Nullable String jarRelativePathOrNull) {
    f_within = within;
    f_declaration = declaration;
    f_positionRelativeToDeclaration = positionRelativeToDeclaration;
    f_eclipseProjectName = DeclUtil.aliasIfPossible(eclipseProjectNameOrNull);
    f_lineNumber = lineNumber > 0 && lineNumber != Integer.MAX_VALUE ? lineNumber : -1;
    f_offset = offset >= 0 && offset != Integer.MAX_VALUE ? offset : -1;
    f_length = length > 0 && length != Integer.MAX_VALUE ? length : -1;
    f_absolutePath = DeclUtil.aliasIfPossible(absolutePathOrNull);
    f_jarRelativePath = DeclUtil.aliasIfPossible(jarRelativePathOrNull);
  }

  @Override
  @Nullable
  public String getAbsolutePathOrNull() {
    return f_absolutePath;
  }

  @Override
  @NonNull
  public final IDecl getDeclaration() {
    return f_declaration;
  }

  @Override
  @NonNull
  public final String getEclipseProjectName() {
    return f_eclipseProjectName == null ? SLUtility.UNKNOWN_PROJECT : f_eclipseProjectName;
  }

  @Override
  @NonNull
  public final String getEclipseProjectNameOrEmpty() {
    return f_eclipseProjectName == null ? "" : f_eclipseProjectName;
  }

  @Override
  @Nullable
  public final String getEclipseProjectNameOrNull() {
    return f_eclipseProjectName;
  }

  @Override
  @Nullable
  public String getRealEclipseProjectNameOrNull() {
    if (f_eclipseProjectName == null)
      return null;
    if (f_eclipseProjectName.startsWith(SLUtility.LIBRARY_PROJECT))
      return null;
    return f_eclipseProjectName;
  }

  @Override
  @Nullable
  public String getJarRelativePathOrNull() {
    return f_jarRelativePath;
  }

  @Override
  public final int getLength() {
    return f_length;
  }

  @Override
  public final int getLineNumber() {
    return f_lineNumber;
  }

  @Override
  public final int getOffset() {
    return f_offset;
  }

  @Override
  @NonNull
  public final String getPackageName() {
    return DeclUtil.getPackageName(f_declaration);
  }

  @Override
  @NonNull
  public Position getPositionRelativeToDeclaration() {
    return f_positionRelativeToDeclaration;
  }

  @Override
  @NonNull
  public final String getSimpleFileName() {
    if (f_within == Within.JAVA_FILE && f_absolutePath != null) {
      final int dotIndex = f_absolutePath.lastIndexOf('.');
      if (dotIndex != -1) {
        final String extension = f_absolutePath.substring(dotIndex);
        if (".java".equals(extension)) {
          final String pathFixed = f_absolutePath.replace('\\', '/');
          final int sepIndex = pathFixed.lastIndexOf('/');
          if (sepIndex == -1)
            return pathFixed;
          else
            return pathFixed.substring(sepIndex + 1);
        }
      }
    }
    // no path, a bad path, or not a .java file -- just guess
    return DeclUtil.guessSimpleFileName(getDeclaration(), f_within);
  }

  @Override
  @NonNull
  public final String getSimpleFileNameWithNoExtension() {
    final String fileNameWithExtension = getSimpleFileName();
    final int dotIndex = fileNameWithExtension.lastIndexOf('.');
    if (dotIndex != -1) {
      return fileNameWithExtension.substring(0, dotIndex);
    }
    return fileNameWithExtension;
  }

  @Override
  @NonNull
  public final String getTypeNameFullyQualified() {
    return DeclUtil.getTypeNameFullyQualified(f_declaration);
  }

  @Override
  @NonNull
  public final String getTypeNameOrNull() {
    return DeclUtil.getTypeNameOrNull(f_declaration);
  }

  @Override
  @NonNull
  public final Within getWithin() {
    return f_within;
  }

  @Override
  public final boolean isFromSource() {
    return f_within == IJavaRef.Within.JAVA_FILE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((f_absolutePath == null) ? 0 : f_absolutePath.hashCode());
    result = prime * result + ((f_declaration == null) ? 0 : f_declaration.hashCode());
    result = prime * result + ((f_eclipseProjectName == null) ? 0 : f_eclipseProjectName.hashCode());
    result = prime * result + ((f_jarRelativePath == null) ? 0 : f_jarRelativePath.hashCode());
    result = prime * result + f_length;
    result = prime * result + f_lineNumber;
    result = prime * result + f_offset;
    result = prime * result + ((f_positionRelativeToDeclaration == null) ? 0 : f_positionRelativeToDeclaration.hashCode());
    result = prime * result + ((f_within == null) ? 0 : f_within.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof JavaRef))
      return false;
    JavaRef other = (JavaRef) obj;
    if (f_absolutePath == null) {
      if (other.f_absolutePath != null)
        return false;
    } else if (!f_absolutePath.equals(other.f_absolutePath))
      return false;
    if (f_declaration == null) {
      if (other.f_declaration != null)
        return false;
    } else if (!f_declaration.equals(other.f_declaration))
      return false;
    if (f_eclipseProjectName == null) {
      if (other.f_eclipseProjectName != null)
        return false;
    } else if (!f_eclipseProjectName.equals(other.f_eclipseProjectName))
      return false;
    if (f_jarRelativePath == null) {
      if (other.f_jarRelativePath != null)
        return false;
    } else if (!f_jarRelativePath.equals(other.f_jarRelativePath))
      return false;
    if (f_length != other.f_length)
      return false;
    if (f_lineNumber != other.f_lineNumber)
      return false;
    if (f_offset != other.f_offset)
      return false;
    if (f_positionRelativeToDeclaration != other.f_positionRelativeToDeclaration)
      return false;
    if (f_within != other.f_within)
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder(this.getClass().getSimpleName());
    b.append('[');
    b.append("projectName=").append(f_eclipseProjectName);
    b.append(",within=").append(f_within);
    b.append(",line=").append(f_lineNumber);
    b.append(",offset=").append(f_offset);
    b.append(",length=").append(f_length);
    b.append(",positionRelativeToDeclaration=").append(f_positionRelativeToDeclaration);
    b.append(",declaration=").append(f_declaration);
    b.append(",absolutePath=").append(f_absolutePath);
    b.append(",jarRelativePath=").append(f_jarRelativePath);
    b.append(']');
    return b.toString();
  }

  @Override
  @NonNull
  public final String encodeForPersistence() {
    /*
     * Make sure this matches the getInstanceFrom() method below!
     * 
     * Also if this is output is changed in any way create a new version prefix.
     * Also note that the getInstanceFrom() method will need to support both the
     * old and new versions of the encoded string.
     */
    return ENCODE_V3 + f_within + "|" + f_positionRelativeToDeclaration + "|"
        + (f_eclipseProjectName == null ? "" : f_eclipseProjectName) + "|" + f_lineNumber + "|" + f_offset + "|" + f_length + "|"
        + (f_absolutePath == null ? "" : f_absolutePath) + "|" + (f_jarRelativePath == null ? "" : f_jarRelativePath) + "|"
        + Decl.encodeForPersistence(f_declaration);
  }

  private static final String ENCODE_V1 = "V1->";
  private static final String ENCODE_V2 = "V2->";
  private static final String ENCODE_V3 = "V3->";

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

    final Pair<Integer, String> versionAndString = getEncodeVersion(encodedForPersistence);
    final int encodeVersion = versionAndString.first();

    if (encodeVersion != -1) {
      final StringBuilder b = new StringBuilder(versionAndString.second());
      final Within within = Within.valueOf(Decl.toNext("|", b));
      String position = Decl.toNext("|", b);
      if (encodeVersion < 3) {
        if ("ON".equals(position))
          position = "ON_DECL";
        if ("WITHIN".equals(position))
          position = "WITHIN_DECL";
      }
      final Position positionRelativeToDeclaration = Position.valueOf(position);
      final String eclipseProjectName = Decl.toNext("|", b);
      final String lineNumberStr = Decl.toNext("|", b);
      final String offsetStr = Decl.toNext("|", b);
      final String lengthStr = Decl.toNext("|", b);
      final String absolutePath;
      final String jarRelativePath;
      if (encodeVersion < 2) {
        absolutePath = jarRelativePath = "";
      } else {
        absolutePath = Decl.toNext("|", b);
        jarRelativePath = Decl.toNext("|", b);
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
   * does not appear to be encoded properly. The string with the version
   * information stripped off is also returned.
   * 
   * @param encodedForPersistence
   *          a text string produced by {@link IJavaRef#encodeForPersistence()}.
   * @return a pair consisting of (a) the encoding version used in the passed
   *         string or -1 if the string does not appear to be encoded properly,
   *         and (b) the string with the version information stripped off.
   */
  private static Pair<Integer, String> getEncodeVersion(@NonNull String encodedForPersistence) {
    if (encodedForPersistence.startsWith(ENCODE_V1))
      return new Pair<>(1, encodedForPersistence.substring(ENCODE_V1.length()));
    else if (encodedForPersistence.startsWith(ENCODE_V2))
      return new Pair<>(2, encodedForPersistence.substring(ENCODE_V2.length()));
    else if (encodedForPersistence.startsWith(ENCODE_V3))
      return new Pair<>(3, encodedForPersistence.substring(ENCODE_V3.length()));
    return new Pair<>(-1, encodedForPersistence);
  }
}
