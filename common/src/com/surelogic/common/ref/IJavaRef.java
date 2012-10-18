package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
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
@Immutable
@ValueObject
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

  /**
   * Gets the absolute path that this code reference is within, or {@code null}
   * if none is available.
   * <p>
   * Note that this information was obtained at the time of the scan and this
   * resource may not exist on the system anymore.
   * 
   * @return an absolute path that this code reference is within, or
   *         {@code null} if none is available.
   */
  @Nullable
  String getAbsolutePathOrNull();

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
   * Gets the path within the <tt>.jar</tt> file returned by
   * {@link #getAbsolutePathOrNull()} that this code reference is within. This
   * method returns {@code null} if this reference is not within a
   * {@link Within#JAR_FILE}.
   * <p>
   * Note that this information was obtained at the time of the scan and this
   * resource may not exist on the system anymore.
   * 
   * @return the path that this code reference is within inside the the
   *         <tt>.jar</tt> file returned by {@link #getAbsolutePathOrNull()}, or
   *         {@code null} if not within a <tt>.jar</tt> file.
   */
  @Nullable
  String getJarRelativePathOrNull();

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
   * Gets the position of this code reference relative to the declaration
   * returned by {@link #getDeclaration()}.
   * 
   * @return the position of this code reference relative to the declaration
   *         returned by {@link #getDeclaration()}.
   */
  @NonNull
  Position getPositionRelativeToDeclaration();

  /**
   * Gets the simple file name of this code reference. It uses the absolute path
   * that this code reference is within or, if none is available, generates the
   * most likely simple file name using this code reference's declaration.
   * <p>
   * A complexity is that if the passed declaration refers to a nested type in a
   * <tt>.java</tt> file, the simple file name matches the outermost declared
   * type name only. This, however, is not always correct because more than one
   * top-level type can be declared within a Java compilation unit. From the
   * declaration information only this is impossible to determine (a
   * {@link IJavaRef} uses the actual path to the compilation unit to overcome
   * this difficulty, see {@link IJavaRef#getSimpleFileName()}).
   * <p>
   * The table below lists some examples of what this method returns. The
   * absolute path value is only shown if it is interesting, in the sense that
   * it changes the behavior of this method.
   * <table border=1>
   * <tr>
   * <th>DeclUtil.getTypeNameFullyQualifiedSureLogic(this.getDeclaration())</th>
   * <th>{@link #getWithin()}</th>
   * <th>{@link #getAbsolutePathOrNull()}</th>
   * <th>return value</th>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Object.java</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Map.java</td>
   * </tr>
   * <tr>
   * <td>util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>C:\Windows\src&#92;util\concurrent\misc\SynchronizationTimer.java</td>
   * <td>SynchronizationTimer.java</td>
   * </tr>
   * <tr>
   * <td>EDU.oswego.cs.dl.util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>/usr/linux/src/util/concurrent/misc/SynchronizationTimer.java</td>
   * <td>SynchronizationTimer.java</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>&nbsp;</td>
   * <td>ClassInDefaultPkg.java</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>&nbsp;</td>
   * <td>package-info.java</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Object.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>C:\Windows\src&#92;util\concurrent\misc\SynchronizationTimer.java</td>
   * <td>ThreadedExecutorRNG.class</td>
   * </tr>
   * <tr>
   * <td>EDU.oswego.cs.dl.util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>/usr/linux/src/util/concurrent/misc/SynchronizationTimer.java</td>
   * <td>ThreadedExecutorRNG.class</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>&nbsp;</td>
   * <td>ClassInDefaultPkg.class</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>&nbsp;</td>
   * <td>package-info.class</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Object.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>&nbsp;</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>C:\Windows\src&#92;util\concurrent\misc\SynchronizationTimer.java</td>
   * <td>ThreadedExecutorRNG.class</td>
   * </tr>
   * <tr>
   * <td>EDU.oswego.cs.dl.util.concurrent.misc/ThreadedExecutorRNG</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>/usr/linux/src/util/concurrent/misc/SynchronizationTimer.java</td>
   * <td>ThreadedExecutorRNG.class</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>&nbsp;</td>
   * <td>ClassInDefaultPkg.class</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>&nbsp;</td>
   * <td>package-info.class</td>
   * </tr>
   * </table>
   * 
   * @return the simple file name of this code reference.
   */
  @NonNull
  String getSimpleFileName();

  /**
   * Gets the simple file name of this code reference with no extension. It
   * simply removes <tt>.java</tt> or <tt>.class</tt> from the result of calling
   * {@link #getSimpleFileName()}.
   * <p>
   * The table below lists some examples of what this method returns.
   * <table border=1>
   * <tr>
   * <th>{@link #getSimpleFileName()}</th>
   * <th>return value</th>
   * </tr>
   * <tr>
   * <td>Object.java</td>
   * <td>Object</td>
   * </tr>
   * <tr>
   * <td>SynchronizationTimer.java</td>
   * <td>SynchronizationTimer</td>
   * </tr>
   * <tr>
   * <td>Map$Entry.class</td>
   * <td>Map$Entry</td>
   * </tr>
   * <tr>
   * <td>package-info.class</td>
   * <td>package-info</td>
   * </tr>
   * </table>
   * 
   * @return the simple file name of this code reference with no extension,
   *         i.e., no <tt>.java</tt> or <tt>.class</tt> suffix.
   */
  @NonNull
  String getSimpleFileNameWithNoExtension();

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
   * Compares the specified object with this reference for equality.
   * 
   * @param o
   *          object to be compared for equality with this reference.
   * @return {@code true} if the specified object is equal to this reference.
   */
  boolean equals(Object o);

  /**
   * Returns the hash code value for this reference.
   * 
   * @return the hash code value for this reference.
   */
  int hashCode();
}
