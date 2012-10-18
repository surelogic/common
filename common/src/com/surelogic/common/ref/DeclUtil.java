package com.surelogic.common.ref;

import java.util.EnumSet;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.ref.IJavaRef.Within;

@Utility
public final class DeclUtil {

  /**
   * Generates the most likely simple file name that the passed declaration is
   * within.
   * <p>
   * A complexity is that if the passed declaration refers to a nested type in a
   * <tt>.java</tt> file, the simple file name matches the outermost declared
   * type name only. This, however, is not always correct because more than one
   * top-level type can be declared within a Java compilation unit. From the
   * declaration information only this is impossible to determine (a
   * {@link IJavaRef} uses the actual path to the compilation unit to overcome
   * this difficulty, see {@link IJavaRef#getSimpleFileName()}).
   * <p>
   * The table below lists some examples of what this method returns.
   * <table border=1>
   * <tr>
   * <th><tt>getTypeNameFullyQualifiedSureLogic(decl)</tt></th>
   * <th><tt>within</tt></th>
   * <th>return value</th>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>Object.java</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>Map.java</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>ClassInDefaultPkg.java</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#JAVA_FILE}</td>
   * <td>package-info.java</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>Object.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>ClassInDefaultPkg.class</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#CLASS_FILE}</td>
   * <td>package-info.class</td>
   * </tr>
   * <tr>
   * <td>java.lang/Object</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>Object.class</td>
   * </tr>
   * <tr>
   * <td>org.apache/Map.Entry</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>Map$Entry.class</td>
   * </tr>
   * <tr>
   * <td>/ClassInDefaultPkg</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>ClassInDefaultPkg.class</td>
   * </tr>
   * <tr>
   * <td>org.apache</td>
   * <td>{@link Within#JAR_FILE}</td>
   * <td>package-info.class</td>
   * </tr>
   * </table>
   * 
   * @param decl
   *          a Java declaration.
   * @param within
   *          what the passed declaration is from&mdash;a Java declaration can
   *          be from a <tt>.java</tt> file, a <tt>.class</tt> file, or a
   *          <tt>.jar</tt> file
   * @return a generated simple file name.
   */
  @NonNull
  public static String guessSimpleFileName(@NonNull final IDecl decl, @NonNull final IJavaRef.Within within) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    if (within == null)
      throw new IllegalArgumentException(I18N.err(44, "within"));

    final StringBuilder b = new StringBuilder();
    final String typeNameDollar = DeclUtil.getTypeNameDollarSignOrNull(decl);
    if (typeNameDollar == null) {
      b.append(SLUtility.PACKAGE_INFO);
    } else {
      b.append(typeNameDollar);
      if (within == Within.JAVA_FILE) {
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
    if (within == Within.JAVA_FILE)
      b.append(".java");
    else
      b.append(".class");
    return b.toString();
  }

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"."</tt>. If the resulting package is the
   * default package the string defined by
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>, <tt>(default package)</tt>
   * 
   * @return the package that the declaration is within.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getPackageName(@NonNull final IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    final IDecl pkgDecl = getRoot(decl);
    if (pkgDecl.getKind() != IDecl.Kind.PACKAGE)
      throw new IllegalArgumentException(I18N.err(285, decl));
    return pkgDecl.getName();
  }

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"."</tt>. If the resulting package is the
   * default package then {@code null} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return the package that the declaration is within, or {@code null} for the
   *         default package.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getPackageNameOrNull(@NonNull final IDecl decl) {
    final String result = getPackageName(decl);
    if (SLUtility.JAVA_DEFAULT_PACKAGE.equals(result))
      return null;
    else
      return result;
  }

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"."</tt>. If the resulting package is the
   * default package then the empty string is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return the package that the declaration is within, or <tt>""</tt> for the
   *         default package.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getPackageNameOrEmpty(@NonNull final IDecl decl) {
    final String result = getPackageName(decl);
    if (SLUtility.JAVA_DEFAULT_PACKAGE.equals(result))
      return "";
    else
      return result;
  }

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"/"</tt>. If the resulting package is the
   * default package then the empty string is returned.
   * <p>
   * Example: <tt>java/util/concurrent/locks</tt>
   * 
   * @return the package that the declaration is within, or <tt>""</tt> for the
   *         default package.
   */
  @NonNull
  public static String getPackageNameSlash(@NonNull final IDecl decl) {
    final String name = getPackageNameOrEmpty(decl);
    return name.replaceAll("\\.", "/");
  }

  /**
   * Gets the Java type name of the passed declaration&mdash;nested types are
   * separated by a <tt>"."</tt>. If the passed declaration is just for a
   * package then {@code null} is returned.
   * <p>
   * <i>Implementation Note:</i> This method uses
   * {@link #getTypeNotInControlFlow(IDecl)} to get the enclosing type.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map.Entry</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the Java type name this declaration is within, or {@code null} if
   *         none.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @Nullable
  public static String getTypeNameOrNull(@NonNull final IDecl decl) {
    IDecl typeDecl = getTypeNotInControlFlow(decl);
    if (typeDecl == null)
      return null; // must be a package declaration
    final StringBuilder b = new StringBuilder(typeDecl.getName());
    while (true) {
      typeDecl = typeDecl.getParent();
      if (typeDecl == null || typeDecl.getKind() == IDecl.Kind.PACKAGE)
        break;
      final String typeName = typeDecl.getName();
      b.insert(0, typeName + ".");
    }
    return b.toString();
  }

  /**
   * Gets the Java type name of the passed declaration&mdash;nested types are
   * separated by a <tt>"."</tt>. If the passed declaration is just for a
   * package then the empty string is returned.
   * <p>
   * <i>Implementation Note:</i> This method uses
   * {@link #getTypeNotInControlFlow(IDecl)} to get the enclosing type.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map.Entry</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the Java type name this declaration is within, or <tt>""</tt> if
   *         none.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @Nullable
  public static String getTypeNameOrEmpty(@NonNull final IDecl decl) {
    final String typeName = getTypeNameOrNull(decl);
    if (typeName == null)
      return "";
    else
      return typeName;
  }

  /**
   * Gets the Java type name of the passed declaration&mdash;nested types are
   * separated by a <tt>"$"</tt>. If the passed declaration is just for a
   * package then {@code null} is returned.
   * <p>
   * This method does not try to build a name for anonymous types or named types
   * declared within control flow. It starts at the first <tt>class</tt>,
   * <tt>enum</tt>, or <tt>interface</tt> declaration that has only parents of
   * those kinds or are a package.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map$Entry</tt>,
   * <tt>AbstractQueuedSynchronizer$ConditionObject</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the Java type name this declaration is within, or {@code null} if
   *         none.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @Nullable
  public static String getTypeNameDollarSignOrNull(@NonNull final IDecl decl) {
    final String typeName = getTypeNameOrNull(decl);
    if (typeName == null)
      return null;
    else
      return typeName.replaceAll("\\.", "\\$");
  }

  /**
   * Gets the kind of the enclosing Java type declaration for the passed
   * declaration. If the passed declaration is just a package declaration then
   * {@link IDecl.Kind#PACKAGE} is returned. Otherwise
   * {@link IDecl.Kind#ANNOTATION}, {@link IDecl.Kind#CLASS},
   * {@link IDecl.Kind#ENUM}, or {@link IDecl.Kind#INTERFACE} are returned
   * depending upon the declaration.
   * <p>
   * <i>Implementation Note:</i> This method uses
   * {@link #getTypeNotInControlFlow(IDecl)} to get the enclosing type.
   * 
   * @param decl
   *          any declaration.
   * @return the kind of the enclosing Java type declaration for the passed
   *         declaration.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static IDecl.Kind getTypeKind(@NonNull final IDecl decl) {
    final IDecl typeDecl = getTypeNotInControlFlow(decl);
    if (typeDecl == null)
      return IDecl.Kind.PACKAGE; // must be a package declaration
    else
      return typeDecl.getKind();
  }

  /**
   * Gets the enclosing Java type declaration for the passed declaration. If the
   * passed declaration is just for a package then {@code null} is returned.
   * <p>
   * This method skips anonymous types or named types declared within control
   * flow&mdash;which for some purposes makes its result to imprecise. It starts
   * at the first <tt>class</tt>, <tt>enum</tt>, or <tt>interface</tt>
   * declaration that has only parents of those kinds or the package.
   * 
   * @param decl
   *          any declaration.
   * @return a Java type declaration or {@code null} if one does not exist.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @Nullable
  public static IDecl getTypeNotInControlFlow(@NonNull final IDecl decl) {
    IDecl typeDecl = getFirstAncestorIn(IDecl.IS_TYPE, decl);
    if (typeDecl == null)
      return null; // must be a package declaration
    while (!getAreAllAncestorsIn(IDecl.IS_PKG_OR_TYPE, typeDecl)) {
      typeDecl = getFirstAncestorIn(IDecl.IS_TYPE, typeDecl.getParent());
    }
    return typeDecl;
  }

  /**
   * Gets the fully qualified Java type name of the passed declaration. Both
   * packages and nested types are separated by a <tt>"."</tt>. If the
   * declaration is just a package then just the package name is returned.
   * <p>
   * Examples: <tt>java.lang.Object</tt>, <tt>java.util.Map.Entry</tt>,
   * <tt>java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>ClassInDefaultPkg</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the fully qualified Java type name that the passed declaration
   *         refers to.
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getTypeNameFullyQualified(@NonNull final IDecl decl) {
    final StringBuilder b = new StringBuilder();
    final String pkgName = getPackageNameOrNull(decl);
    if (pkgName != null) {
      b.append(pkgName);
    }
    final String typeName = getTypeNameOrNull(decl);
    if (typeName != null) {
      if (pkgName != null)
        b.append('.'); // dot only if not in the default package
      b.append(typeName);
    }
    return b.toString();
  }

  /**
   * Gets the fully qualified Java type name of the passed declaration&mdash;but
   * only includes the outermost enclosing type (no nested types). Both packages
   * and nested types are separated by a <tt>"."</tt>. If the declaration is
   * just a package then just the package name is returned.
   * <p>
   * Examples: <tt>java.lang.Object</tt>, <tt>java.util.Map</tt>,
   * <tt>java.util.concurrent.locks.ReentrantReadWriteLock</tt>,
   * <tt>ClassInDefaultPkg</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the fully qualified Java type name that the passed declaration
   *         refers to.
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getTypeNameFullyQualifiedOutermostTypeNameOnly(@NonNull final IDecl decl) {
    final StringBuilder b = new StringBuilder();
    final String pkgName = getPackageNameOrNull(decl);
    if (pkgName != null) {
      b.append(pkgName);
    }
    IDecl outerType = getLastAncestorIn(IDecl.IS_TYPE, decl);
    if (outerType != null) {
      final String typeName = outerType.getName();
      if (typeName != null) {
        if (pkgName != null)
          b.append('.'); // dot only if not in the default package
        b.append(typeName);
      }
    }
    return b.toString();
  }

  /**
   * Gets the fully qualified Java type name of the passed declaration in a
   * particular SureLogic format. Nested package names are separated by
   * <tt>"."</tt>, the package name is separated from the type name by a "/",
   * and nested type names are separated by <tt>"."</tt>. The "/" must always
   * appear&mdash;even if the type is in the default package or just a package
   * name is being returned.
   * <p>
   * Examples: <tt>java.lang/Object</tt>, <tt>java.util/Map.Entry</tt>,
   * <tt>java.util.concurrent.locks/ReentrantReadWriteLock.ReadLock</tt>,
   * <tt>/ClassInDefaultPkg</tt>
   * 
   * @param decl
   *          any declaration.
   * @return the fully qualified Java type name that the passed declaration
   *         refers to in the SureLogic format.
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static String getTypeNameFullyQualifiedSureLogic(@NonNull final IDecl decl) {
    final StringBuilder b = new StringBuilder(getPackageNameOrEmpty(decl));
    b.append('/');
    b.append(getTypeNameOrEmpty(decl));
    return b.toString();
  }

  /**
   * Finds the root declaration of any declaration.
   * 
   * @param decl
   *          any declaration.
   * 
   * @return a declaration. If the passed declaration tree is well-formed it
   *         should be of {@link IDecl.Kind#PACKAGE}.
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null.
   */
  @NonNull
  public static IDecl getRoot(@NonNull final IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    // find the root
    IDecl root = decl;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return root;
  }

  /**
   * Returns the first enclosing parent declaration, toward the root, that
   * matches the passed kind. If the passed declaration matches it is returned.
   * 
   * @param kind
   *          a kind of declaration.
   * @param decl
   *          any declaration.
   * @return the first parent declaration of the passed declaration that is of
   *         the passed kind, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getFirstAncestorOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (node.getKind() == kind)
        return node;
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the first enclosing parent declaration, toward the root, that is
   * <i>not</i> of the passed kind. If the passed declaration matches it is
   * returned.
   * 
   * @param kind
   *          a kind of declaration.
   * @param decl
   *          any declaration.
   * @return the first parent declaration of the passed declaration that is not
   *         of the passed kind, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getFirstAncestorNotOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (node.getKind() != kind)
        return node;
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the first enclosing parent declaration, toward the root, that is in
   * the passed set of kinds. If the passed declaration matches it is returned.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return the first parent declaration of the passed declaration that is in
   *         the passed set of kinds, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getFirstAncestorIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (kinds.contains(node.getKind()))
        return node;
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the first enclosing parent declaration, toward the root, that is
   * <i>not</i> in the passed set of kinds. If the passed declaration matches it
   * is returned.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return the first parent declaration of the passed declaration that is not
   *         in the passed set of kinds, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getFirstAncestorNotIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (!kinds.contains(node.getKind()))
        return node;
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the last enclosing parent declaration, prior to the root, that
   * matches the passed kind. If the passed declaration matches it is returned.
   * 
   * @param kind
   *          a kind of declaration.
   * @param decl
   *          any declaration.
   * @return the last parent declaration of the passed declaration that is of
   *         the passed kind, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getLastAncestorOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (node.getKind() == kind) {
        final IDecl parent = node.getParent();
        if (parent == null) {
          return node;
        } else {
          if (getAreAllAncestorsNotOfKind(kind, parent))
            return node;
        }
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the last enclosing parent declaration, prior to the root, that is
   * <i>not</i> of the passed kind. If the passed declaration matches it is
   * returned.
   * 
   * @param kind
   *          a kind of declaration.
   * @param decl
   *          any declaration.
   * @return the last parent declaration of the passed declaration that is not
   *         of the passed kind, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getLastAncestorNotOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (node.getKind() != kind) {
        final IDecl parent = node.getParent();
        if (parent == null) {
          return node;
        } else {
          if (getAreAllAncestorsOfKind(kind, parent))
            return node;
        }
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the last enclosing parent declaration, prior to the root, that is
   * in the passed set of kinds. If the passed declaration matches it is
   * returned.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return the last parent declaration of the passed declaration that is in
   *         the passed set of kinds, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getLastAncestorIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (kinds.contains(node.getKind())) {
        final IDecl parent = node.getParent();
        if (parent == null) {
          return node;
        } else {
          if (getAreAllAncestorsNotIn(kinds, parent))
            return node;
        }
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Returns the last enclosing parent declaration, prior to the root, that is
   * <i>not</i> in the passed set of kinds. If the passed declaration matches it
   * is returned.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return the last parent declaration of the passed declaration that is not
   *         in the passed set of kinds, or {@code null} if none can be found.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  @Nullable
  public static IDecl getLastAncestorNotIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (!kinds.contains(node.getKind())) {
        final IDecl parent = node.getParent();
        if (parent == null) {
          return node;
        } else {
          if (getAreAllAncestorsIn(kinds, parent))
            return node;
        }
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * Gets if the passed declaration and all its ancestor declarations are of the
   * passed kind.
   * 
   * @param kind
   *          a kind.
   * @param decl
   *          any declaration.
   * @return {@code true} if the passed declaration and all its ancestor
   *         declarations are of the passed kind, {@code false} otherwise.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  public static boolean getAreAllAncestorsOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (!(node.getKind() == kind))
        return false;
      node = node.getParent();
    }
    return true;
  }

  /**
   * Gets if the passed declaration and all its ancestor declarations are
   * <i>not</i> of the passed kind.
   * 
   * @param kind
   *          a kind.
   * @param decl
   *          any declaration.
   * @return {@code true} if the passed declaration and all its ancestor
   *         declarations are <i>not</i> of the passed kind, {@code false}
   *         otherwise.
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  public static boolean getAreAllAncestorsNotOfKind(@NonNull final IDecl.Kind kind, @NonNull final IDecl decl) {
    if (kind == null)
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (node.getKind() == kind)
        return false;
      node = node.getParent();
    }
    return true;
  }

  /**
   * Gets if the passed declaration and its ancestor declarations are contained
   * in the passed set of kinds.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return {@code true} if for all <tt>d</tt> in this declaration and all its
   *         any declaration. ancestor declarations: <tt>kinds.contains(d)</tt>
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  public static boolean getAreAllAncestorsIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (!kinds.contains(node.getKind()))
        return false;
      node = node.getParent();
    }
    return true;
  }

  /**
   * Gets if the passed declaration and its ancestor declarations are <i>not</i>
   * contained in the passed set of kinds.
   * 
   * @param kinds
   *          a set of kinds.
   * @param decl
   *          any declaration.
   * @return {@code true} if for all <tt>d</tt> in this declaration and all its
   *         any declaration. ancestor declarations: <tt>!kinds.contains(d)</tt>
   * 
   * @throws IllegalArgumentException
   *           if any argument is {@code null}.
   */
  public static boolean getAreAllAncestorsNotIn(@NonNull final EnumSet<IDecl.Kind> kinds, @NonNull final IDecl decl) {
    if (kinds == null)
      throw new IllegalArgumentException(I18N.err(44, "kinds"));
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl node = decl;
    while (node != null) {
      if (kinds.contains(node.getKind()))
        return false;
      node = node.getParent();
    }
    return true;
  }

  private enum FuncUnparse {
    USE_NEW, USE_TYPE, ONLY_PARAMS
  }

  private static String getSignature(IDeclFunction func, FuncUnparse kind, boolean fullyQualifyParameters) {
    if (func == null) {
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    }
    if (kind == null) {
      throw new IllegalArgumentException(I18N.err(44, "kind"));
    }
    final StringBuilder sb;
    if (kind == FuncUnparse.ONLY_PARAMS) {
      sb = new StringBuilder();
    } else {
      String name = func.getName();
      if (name == null) {
        if (kind == FuncUnparse.USE_TYPE) {
          name = func.getParent().getName();
        } else {
          name = "new";
        }
      }
      sb = new StringBuilder(name);
      sb.append('(');
    }

    boolean first = true;
    for (IDeclParameter p : func.getParameters()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      final TypeRef t = p.getTypeOf();
      sb.append(fullyQualifyParameters ? t.getFullyQualified() : t.getCompact());
    }
    if (kind != FuncUnparse.ONLY_PARAMS) {
      sb.append(')');
    }
    return sb.toString();
  }

  /**
   * As it would appear in source code
   */
  public static String getSignature(IDeclFunction func) {
    return getSignature(func, FuncUnparse.USE_TYPE, false);
  }

  public static String getParametersFullyQualified(IDeclFunction func) {
    return getSignature(func, FuncUnparse.ONLY_PARAMS, true);
  }
}
