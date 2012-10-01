package com.surelogic.common;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;

/**
 * Interface for referencing a location in Java code&mdash;source or binary.
 */
@ThreadSafe
public interface IJavaRef {

  enum Within {
    JAVA_FILE, CLASS_FILE, JAR_FILE
  }

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
   * Gets a relative path to the resource that this refers to. The result is
   * different depending upon what this refers to is {@link #Within}.
   * <ul>
   * <li>If {@link #getWithin()} == {@link Within#JAVA_FILE} then the relative
   * path to the <tt>.java</tt> file from the workspace root is returned.
   * Example:
   * <tt>"PlanetBaron/src/org/planetbaron/protocol/parser/ParseException.java"</tt>
   * </li>
   * <li>If {@link #getWithin()} == {@link Within#CLASS_FILE} then the relative
   * path to the <tt>.class</tt> file from the workspace root is returned.
   * Example: <tt>"PlanetBaron/classfolder/org/test/TestHelper.class"</tt></li>
   * <li>If {@link #getWithin()} == {@link Within#JAR_FILE} then the result is
   * the path to the file within the JAR file. Examples:
   * <tt>java/lang/Object.class</tt>,
   * <tt>java/util/concurrent/locks/AbstractQueuedSynchronizer$ConditionObject.class</tt>, <tt>ClassInDefaultPkg.class</tt></li>
   * </ul>
   * 
   * @return a relative path to the resource that this refers to.
   */
  @NonNull
  String getRelativePath();

  /**
   * Gets the name of the file that this refers to. This is just the last name
   * in the pathname's name sequence. The file is within a JAR file if
   * {@link #getWithin()} == {@link Within#JAR_FILE}, otherwise it should exist
   * in the file system (unless it has been deleted).
   * <p>
   * Examples: <tt>ParseException.java</tt>, <tt>TestHelper.class</tt>,
   * <tt>Object.class</tt>,
   * <tt>AbstractQueuedSynchronizer$ConditionObject.class</tt>
   * 
   * @return the name of the file that this refers to.
   */
  @NonNull
  String getFileName();

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
   * @return the Eclipse project name or library reference that this refers to.
   */
  @NonNull
  String getEclipseProjectName();

  /**
   * Gets the Java package name that this refers to. If the resulting package is
   * the default package, or no package, the string defined by
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE} is returned. The package must be
   * known for a reference to be valid, so this should always return something
   * reasonable.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return The package that the source is within.
   */
  @NonNull
  String getPackageName();

  /**
   * Gets the Java type name that this refers to. Nested types are separated by
   * a <tt>"."</tt>. The type name must be known for a reference to be valid, so
   * this should always return something reasonable.
   * <p>
   * Examples: <tt>Object</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>
   * 
   * @return the Java type name that this refers to.
   */
  @NonNull
  String getTypeName();

  /**
   * Gets the "type" of Java type that this refers to: <tt>class</tt>,
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
   * Examples: <tt>java.lang.Object</tt>,
   * <tt>java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock</tt>
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
   * names are separated by <tt>"$"</tt>. The type name and package must be
   * known for a reference to be valid, so this should always return something
   * reasonable.
   * <p>
   * Examples: <tt>java.lang/Object</tt>, <tt>java.util/Map$Entry</tt>,
   * <tt>java.util.concurrent.locks/ReentrantReadWriteLock$ReadLock</tt>,
   * <tt>/ClassInDefaultPkg</tt>
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
