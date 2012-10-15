package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.SLUtility;
import com.surelogic.common.ref.IDecl.Kind;
import com.surelogic.common.ref.JavaRef.Builder;

/**
 * Interface for referencing a location in Java code&mdash;source or binary.
 * <p>
 * Concrete instances or this interface are constructed using
 * {@link JavaRef.Builder}&mdash;copy-and-modify is supported via
 * {@link Builder#Builder(IJavaRef)}.
 * <p>
 * Encoding into a text string (via {@link #encodeForPersistence()}) and
 * decoding back into an instance (via
 * {@link JavaRef#parseEncodedForPersistence(String)}) are supported, for
 * example, to persist to and restore from an XML attribute.
 */
@ThreadSafe
public interface IJavaRef {

  /**
   * A referenced location in Java code can be within a <tt>.java</tt> file, a
   * <tt>.class</tt> file, or a <tt>.jar</tt> file.
   * 
   */
  enum Within {
    /**
     * Indicates the code reference is in a <tt>.java</tt> file.
     */
    JAVA_FILE,
    /**
     * Indicates the code reference is in a <tt>.class</tt> file.
     */
    CLASS_FILE,
    /**
     * Indicates the code reference is in a <tt>.class</tt> file within a
     * <tt>.jar</tt> file.
     */
    JAR_FILE
  }

  /**
   * A referenced location in Java code can be within or on a particular Java
   * declaration.
   */
  enum Position {
    /**
     * Indicates the code reference is <i>on</i> the declaration.
     */
    ON,
    /**
     * Indicates the code reference is on the declaration's receiver. This only
     * makes sense if the declaration is {@link Kind#METHOD} or
     * {@link Kind#CONSTRUCTOR}.
     */
    ON_RECEIVER,
    /**
     * Indicates the code reference is on the declaration's return value. This
     * only makes sense if the declaration is {@link Kind#METHOD} or
     * {@link Kind#CONSTRUCTOR}.
     */
    ON_RETURN_VALUE,
    /**
     * Indicates the code reference is <i>within</i> the declaration.
     */
    WITHIN,
  }

  /**
   * Gets the type of resource that this reference is within.
   * 
   * @return the type of resource that this reference is within.
   */
  @NonNull
  Within getWithin();

  /**
   * Gets if this refers to source code. This is a convenience method defined to
   * be the same as <tt>({@link #getWithin()} == {@link Within#JAVA_FILE})</tt>.
   * 
   * @return {@code true} if this refers to source code, {@code false}
   *         otherwise.
   */
  boolean isFromSource();

  /**
   * Gets the line number of the code snippet this refers to, or <tt>-1</tt> if
   * unknown.
   * <p>
   * The result is only valid if {@link #isFromSource()} is {@code true}.
   * 
   * @return the line number of the code snippet this refers to, or <tt>-1</tt>
   *         if unknown.
   */
  int getLineNumber();

  /**
   * Returns the character offset, from the start of the file, to the start of
   * the code snippet this refers to, or <tt>-1</tt> if unknown.
   * <p>
   * The result is only valid if {@link #isFromSource()} is {@code true}.
   * 
   * @return the character offset, from the start of the file, to the start of
   *         the code snippet this refers to, or <tt>-1</tt> if unknown.
   * @see #getLength()
   */
  int getOffset();

  /**
   * Gets the length, in characters, of the code snippet this refers to, or
   * <tt>-1</tt> if unknown.
   * <p>
   * The result is only valid if {@link #isFromSource()} is {@code true}.
   * 
   * @return the length, in characters, of the code snippet this refers to, or
   *         <tt>-1</tt> if unknown.
   * @see #getOffset()
   */
  int getLength();

  /**
   * Gets the Eclipse project name or library reference (shared between
   * projects) that this refers to. If the Eclipse project name is unknown, for
   * any reason, the string defined by {@link SLUtility#UNKNOWN_PROJECT} is
   * returned.
   * <p>
   * Examples: <tt>PlanetBaron</tt>,
   * <tt>org.eclipse.jdt.launching.JRE_CONTAINER</tt>,
   * <tt>(unknown project)</tt>
   * 
   * @return the Eclipse project name or library reference that this refers to,
   *         or {@link SLUtility#UNKNOWN_PROJECT} if unknown.
   */
  @NonNull
  String getEclipseProjectName();

  /**
   * Gets the Eclipse project name or library reference (shared between
   * projects) that this refers to. If the Eclipse project name is unknown, for
   * any reason, the empty string is returned.
   * <p>
   * Examples: <tt>PlanetBaron</tt>,
   * <tt>org.eclipse.jdt.launching.JRE_CONTAINER</tt>
   * 
   * @return the Eclipse project name or library reference that this refers to,
   *         or <tt>""</tt> if unknown.
   */
  @NonNull
  String getEclipseProjectNameOrEmpty();

  /**
   * Gets the Eclipse project name or library reference (shared between
   * projects) that this refers to. If the Eclipse project name is unknown, for
   * any reason, then {@code null} is returned.
   * <p>
   * Examples: <tt>PlanetBaron</tt>,
   * <tt>org.eclipse.jdt.launching.JRE_CONTAINER</tt>
   * 
   * @return the Eclipse project name or library reference that this refers to,
   *         or {@code null} if unknown.
   */
  @Nullable
  String getEclipseProjectNameOrNull();

  /**
   * Gets the Java declaration that this code reference is on or within. To
   * determine if the code reference is on or within the declaration use
   * {@link #isOnDeclaration()}.
   * <p>
   * Many helpful methods to pull information from a declaration are provided in
   * {@link DeclUtil}. This type only wraps a small subset of them.
   * 
   * @return the Java declaration that this code reference is on or within.
   * 
   * @see IDecl
   * @see DeclUtil
   */
  @NonNull
  IDecl getDeclaration();

  /**
   * Gets the position of this code reference relative to the declaration
   * returned by {@link #getDeclaration()}.
   * 
   * @return the position of this code reference relative to the declaration
   *         returned by {@link #getDeclaration()}.
   */
  @NonNull
  Position getPositionRelativeToDeclaration();

  /**
   * Gets the Java package name that this refers to&mdash;nested package names
   * are separated by a <tt>"."</tt>. If the resulting package is the default
   * package, or no package, the string defined by
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE} is returned.
   * <p>
   * This method has the same effect as calling
   * {@link DeclUtil#getPackageName(IDecl)} and passing the result of
   * {@link #getDeclaration()}. The {@link DeclUtil} has several other helpful
   * methods to return names in various forms.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>, <tt>(default package)</tt>
   * 
   * @return The package that the source is within.
   */
  @NonNull
  String getPackageName();

  /**
   * Gets the Java type name that this refers to&mdash;nested types are
   * separated by a <tt>"."</tt>.
   * <p>
   * This method has the same effect as calling
   * {@link DeclUtil#getTypeNameOrNull(IDecl)} and passing the result of
   * {@link #getDeclaration()}. The {@link DeclUtil} has several other helpful
   * methods to return names in various forms.
   * <p>
   * Examples: <tt>Object</tt>, <tt>Map.Entry</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>
   * 
   * @return the Java type name that this refers to.
   */
  @Nullable
  String getTypeNameOrNull();

  /**
   * Java type name that this refers to. Both packages and nested types are
   * separated by a <tt>"."</tt>. If the declaration is just a package then just
   * the package name is returned.
   * <p>
   * This method has the same effect as calling
   * {@link DeclUtil#getTypeNameFullyQualified(IDecl)} and passing the result of
   * {@link #getDeclaration()}. The {@link DeclUtil} has several other helpful
   * methods to return names in various forms.
   * <p>
   * Examples: <tt>java.lang.Object</tt>, <tt>java.util.Map.Entry</tt>,
   * <tt>java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>ClassInDefaultPkg</tt>
   * 
   * @return the fully qualified Java type name that this refers to.
   */
  @NonNull
  String getTypeNameFullyQualified();

  @Deprecated
  @Nullable
  String getJavaId();

  @Deprecated
  @Nullable
  String getEnclosingJavaId();

  @NonNull
  Long getHash();

  /**
   * Gets an encoded text string that represents the data in this code
   * reference. It is suitable for persistence, for example, in an XML
   * attribute. The returned string can be restored to a code reference via
   * {@link JavaRef#parseEncodedForPersistence(String)}.
   * 
   * @return an encoded text string that represents the data in this code
   *         reference.
   * @see JavaRef#parseEncodedForPersistence(String)
   */
  @NonNull
  String encodeForPersistence();
}
