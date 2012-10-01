package com.surelogic.common;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.JavaRef.Builder;

/**
 * Interface for referencing a location in Java code&mdash;source or binary.
 * <p>
 * Concrete instances or this interface are constructed using
 * {@link JavaRef.Builder} &mdash;copy-and-modify is supported via
 * {@link Builder#Builder(IJavaRef)}.
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

  @Nullable
  String getJavaId();

  @Nullable
  String getEnclosingJavaId();

  @NonNull
  Long getHash();
}
