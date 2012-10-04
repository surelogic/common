package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.SLUtility;
import com.surelogic.common.ref.JavaRef.Builder;

/**
 * Interface for referencing a location in Java code&mdash;source or binary.
 * <p>
 * Concrete instances or this interface are constructed using
 * {@link JavaRef.Builder}&mdash;copy-and-modify is supported via
 * {@link Builder#Builder(IJavaRef)}.
 * <p>
 * Encoding into a text string (via {@link #encodeForPersistence()}) and
 * decoding back into an instance (via {@link JavaRef#getInstanceFrom(String)})
 * are supported, for example, to persist to and restore from an XML attribute.
 */
@ThreadSafe
public interface IJavaRef {

  /**
   * A referenced location in Java code can be within a <tt>.java</tt> file, a
   * <tt>.class</tt> file, or a <tt>.jar</tt> file.
   * 
   */
  enum Within {
    JAVA_FILE, CLASS_FILE, JAR_FILE
  }

  /**
   * A Java type can be either a <tt>class</tt>, an <tt>enum</tt>, or an
   * <tt>interface</tt>.
   */
  enum TypeType {
    CLASS, ENUM, INTERFACE
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
   * <tt>org.eclipse.jdt.launching.JRE_CONTAINER</tt>
   * 
   * @return the Eclipse project name or library reference that this refers to,
   *         or {@link SLUtility#UNKNOWN_PROJECT} if unknown.
   */
  @NonNull
  String getEclipseProjectName();

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
   * Gets the Java package name that this refers to&mdash;nested package names
   * are separated by a <tt>"."</tt>. If the resulting package is the default
   * package, or no package, the string defined by
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return The package that the source is within.
   */
  @NonNull
  String getPackageName();

  /**
   * Gets the Java package name that this refers to&mdash;nested package names
   * are separated by a <tt>"."</tt>. If the resulting package is the default
   * package, or no package, then {@code null} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return The package that the source is within, or {@code null} for the
   *         default package.
   */
  @Nullable
  String getPackageNameOrNull();

  /**
   * Gets the Java package name that this refers to&mdash;nested package names
   * are separated by a <tt>"/"</tt>. If the resulting package is the default
   * package, or no package, the empty string is returned.
   * <p>
   * Example: <tt>java/util/concurrent/locks</tt>
   * 
   * @return The package that the source is within, or <tt>""</tt> for the
   *         default package.
   */
  @NonNull
  String getPackageNameSlash();

  /**
   * Gets the Java type name that this refers to&mdash;nested types are
   * separated by a <tt>"."</tt>. The type name must be known for a reference to
   * be valid, so this should always return something reasonable.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map.Entry</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>, <tt>package-info</tt>
   * 
   * @return the Java type name that this refers to.
   */
  @NonNull
  String getTypeName();

  /**
   * Gets the Java type name that this refers to&mdash;nested types are
   * separated by a <tt>"$"</tt>. The type name must be known for a reference to
   * be valid, so this should always return something reasonable.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map$Entry</tt>,
   * <tt>AbstractQueuedSynchronizer$ConditionObject</tt>, <tt>package-info</tt>
   * 
   * @return the Java type name that this refers to.
   */
  @NonNull
  String getTypeNameDollarSign();

  /**
   * Gets the type of Java type that this refers to: <tt>class</tt>,
   * <tt>enum</tt>, or <tt>interface</tt>.
   * 
   * @return the type of Java type that this refers to.
   */
  @NonNull
  TypeType getTypeType();

  /**
   * Gets the fully qualified Java type name that this refers to. Both packages
   * and nested types are separated by a <tt>"."</tt>. The type name and package
   * must be known for a reference to be valid, so this should always return
   * something reasonable.
   * <p>
   * <tt>package-info</tt> files are handled as if they were a class. They can
   * exist as a <tt>.java</tt> or <tt>.class</tt>.
   * <p>
   * Examples: <tt>java.lang.Object</tt>, <tt>java.util.Map.Entry</tt>,
   * <tt>java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>ClassInDefaultPkg</tt>, <tt>edu.afit.smallworld.package-info</tt>
   * 
   * @return the fully qualified Java type name that this refers to.
   */
  @NonNull
  String getTypeNameFullyQualified();

  /**
   * Gets the fully qualified Java type name that this refers to in a particular
   * SureLogic format. Nested package names are separated by <tt>"."</tt>, the
   * package name is separated from the type name by a "/" (which must always
   * appear&mdash;even if the type is in the default package), and nested type
   * names are separated by <tt>"."</tt>. The type name and package must be
   * known for a reference to be valid, so this should always return something
   * reasonable.
   * <p>
   * <tt>package-info</tt> files are handled as if they were a class. They can
   * exist as a <tt>.java</tt> or <tt>.class</tt>.
   * <p>
   * Examples: <tt>java.lang/Object</tt>, <tt>java.util/Map.Entry</tt>,
   * <tt>java.util.concurrent.locks/ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>/ClassInDefaultPkg</tt>, <tt>edu.afit.smallworld/package-info</tt>
   * 
   * @return the fully qualified Java type name that this refers to in the
   *         SureLogic format.
   */
  @NonNull
  String getTypeNameFullyQualifiedSureLogic();

  /**
   * Generates the most likely simple file name that this refers to. For a full
   * pathname relative to a point on the Java classpath use
   * {@link #getClasspathRelativePathname()}.
   * <p>
   * A complexity is that if this refers to a nested type in a <tt>.java</tt>
   * file the file is named according to the outermost type name only.
   * <p>
   * The table below lists some examples of what this method returns.
   * <table border=1>
   * <tr>
   * <th>{@link #getTypeNameFullyQualifiedSureLogic()}</th>
   * <th>{@link #getWithin()}</th>
   * <th>{@link #getSimpleFileName()}</th>
   * </tr>
   * <tr>
   * <td>com.surelogic/Editor</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>Editor.java</td>
   * </tr>
   * <tr>
   * <td>com.surelogic/Editor.Builder</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>Editor.java</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>ClassInDefaultPkg.java</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>Object.class</td>
   * </tr>
   * <tr>
   * <td>java.util/Map.Entry</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * </table>
   * 
   * @return a generated simple file name.
   * 
   * @see #getClasspathRelativePathname()
   */
  @NonNull
  String getSimpleFileName();

  /**
   * Generates the most likely relative path and file name that this refers to.
   * The returned pathname is relative to a point on the Java classpath. This
   * may be a path on the filesystem or within a JAR file (to find out call
   * {@link #getWithin()}). To obtain just the simple file name use
   * {@link #getSimpleFileName()}.
   * <p>
   * A complexity is that if this refers to a nested type in a <tt>.java</tt>
   * file the file is named according to the outermost type name only.
   * <p>
   * The table below lists some examples of what this method returns.
   * <table border=1>
   * <tr>
   * <th>{@link #getTypeNameFullyQualifiedSureLogic()}</th>
   * <th>{@link #getWithin()}</th>
   * <th>{@link #getClasspathRelativePathname()}</th>
   * </tr>
   * <tr>
   * <td>com.surelogic/Editor</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>com/surelogic/Editor.java</td>
   * </tr>
   * <tr>
   * <td>com.surelogic/Editor.Builder</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>com/surelogic/Editor.java</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>ClassInDefaultPkg.java</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>java/lang/Object.class</td>
   * </tr>
   * <tr>
   * <td>java.util/Map.Entry</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>java/util/Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>org/apache/Map$Entry.class</td>
   * </tr>
   * </table>
   * 
   * @return a generated path and file name relative to a point on the Java
   *         classpath.
   */
  @NonNull
  String getClasspathRelativePathname();

  @Nullable
  String getJavaId();

  @Nullable
  String getEnclosingJavaId();

  @NonNull
  Long getHash();

  /**
   * Gets an encoded text string that represents the data in this code
   * reference. It is suitable for persistence, for example, in an XML
   * attribute. The returned string can be restored to a code reference via
   * {@link JavaRef#getInstanceFrom(String)}.
   * 
   * @return an encoded text string that represents the data in this code
   *         reference.
   * @see JavaRef#getInstanceFrom(String)
   */
  @NonNull
  String encodeForPersistence();
}
