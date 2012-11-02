package com.surelogic.common.ref;

import java.util.EnumSet;
import java.util.List;

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

  @NonNull
  public static String toString(@NonNull final IDecl decl) {
    final class ToStringVisitor extends DeclVisitor {
      final StringBuilder b = new StringBuilder();

      @Override
      public boolean visitClass(IDeclType node) {
        b.append('.');
        if (node.getVisibility() == IDecl.Visibility.ANONYMOUS)
          b.append("(anonymous " + node.getPosition() + ")");
        else {
          b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(' ');
          if (node.isStatic())
            b.append("static ");
          if (node.isAbstract())
            b.append("abstract ");
          if (node.isFinal())
            b.append("final ");
          b.append("class\u00bb");
          b.append(node.getName());
        }
        return true;
      }

      @Override
      public boolean visitInterface(IDeclType node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(" interface\u00bb");
        b.append(node.getName());
        return true;
      }

      @Override
      public void visitAnnotation(IDeclType node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(" annotation\u00bb");
        b.append(node.getName());
      }

      @Override
      public void visitEnum(IDeclType node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(" enum\u00bb");
        b.append(node.getName());
      }

      @Override
      public void visitField(IDeclField node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(' ');
        if (node.isStatic())
          b.append("static ");
        if (node.isFinal())
          b.append("final ");
        b.append(node.getTypeOf().getCompact()).append('\u00bb');
        b.append(node.getName());
      }

      @Override
      public void visitInitializer(IDecl node) {
        b.append('.');
        b.append('(');
        if (node.isStatic()) {
          b.append("static ");
        }
        b.append("initializer)");
      }

      @Override
      public boolean visitMethod(IDeclFunction node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(' ');
        if (node.isStatic())
          b.append("static ");
        if (node.isAbstract())
          b.append("abstract ");
        if (node.isFinal())
          b.append("final ");
        final List<IDeclTypeParameter> typeParameters = node.getTypeParameters();
        if (!typeParameters.isEmpty()) {
          visitTypeParameters(typeParameters);
          b.append(' ');
        }
        if (node.getTypeOf() == null)
          b.append("void");
        else
          b.append(node.getTypeOf().getCompact());
        b.append('\u00bb');
        b.append(node.getName());
        final List<IDeclParameter> parameters = node.getParameters();
        if (parameters.isEmpty())
          b.append("()");
        else
          visitParameters(parameters);
        return false;
      }

      @Override
      public boolean visitConstructor(IDeclFunction node) {
        b.append('.');
        b.append('\u00ab').append(node.getVisibility().toString().toLowerCase()).append(' ');
        if (node.isImplicit())
          b.append("implicit ");
        final List<IDeclTypeParameter> typeParameters = node.getTypeParameters();
        if (!typeParameters.isEmpty()) {
          visitTypeParameters(typeParameters);
          b.append(' ');
        }
        b.append("constructor\u00bb");
        b.append(node.getName());
        final List<IDeclParameter> parameters = node.getParameters();
        if (parameters.isEmpty())
          b.append("()");
        else
          visitParameters(parameters);
        return false;
      }

      @Override
      public boolean visitParameters(List<IDeclParameter> parameters) {
        b.append(toStringParametersHelper(parameters, false, true, true));
        return false;
      }

      @Override
      public void visitParameter(IDeclParameter node, boolean partOfDecl) {
        if (partOfDecl) {
          b.append("(parameter ").append(node.getPosition());
          b.append(" \u00ab");
          b.append(toStringParameterHelper(node, false, true, true));
          b.append("\u00bb)");
        }
      }

      @Override
      public boolean visitTypeParameters(List<IDeclTypeParameter> typeParameters) {
        b.append(toStringTypeParametersHelper(typeParameters));
        return false;
      }

      @Override
      public void visitTypeParameter(IDeclTypeParameter node, boolean partOfDecl) {
        if (partOfDecl) {
          b.append("(type parameter ").append(node.getPosition());
          b.append(" \u00ab");
          b.append(toStringTypeParameterHelper(node));
          b.append("\u00bb)");
        }
      }

      @Override
      public void visitPackage(IDeclPackage node) {
        b.append(node.getName());
      }

      @Override
      public String toString() {
        return b.toString();
      }
    }
    final ToStringVisitor v = new ToStringVisitor();
    decl.acceptRootToThis(v);
    return v.toString();
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

  @Nullable
  public static String getEclipseJavaOutlineLikeLabel(@Nullable final IDecl decl) {
    if (decl == null)
      return null;
    switch (decl.getKind()) {
    case PACKAGE:
      return decl.getName();
    case ANNOTATION:
    case CLASS:
    case INTERFACE:
    case ENUM:
      if (decl.getVisibility() == IDecl.Visibility.ANONYMOUS) {
        return "new " + decl.getTypeOf().getCompact() + "() {...}";
      } else
        return decl.getName() + toStringTypeParametersHelper(decl.getTypeParameters());
    case INITIALIZER:
      return "{...}";
    case CONSTRUCTOR:
    case METHOD: {
      final StringBuilder b = new StringBuilder();
      b.append(decl.getName());
      b.append(toStringParametersHelper(decl.getParameters(), false, false, false));
      if (!decl.getTypeParameters().isEmpty()) {
        b.append(' ');
        b.append(toStringTypeParametersHelper(decl.getTypeParameters()));
      }
      if (decl.getKind() == IDecl.Kind.METHOD) {
        b.append(" : ");
        b.append(decl.getTypeOf() == null ? "void" : decl.getTypeOf().getCompact());
      }
      return b.toString();
    }
    case FIELD:
    case PARAMETER:
      return decl.getName() + " : " + decl.getTypeOf().getCompact();
    case TYPE_PARAMETER:
      return toStringTypeParameterHelper((IDeclTypeParameter) decl);
    }
    return null;
  }

  /**
   * Gets a string representation of the method or constructor declaration with
   * no return type.
   * <p>
   * Examples: <tt>Foo(Object, String)</tt> <tt>Object()</tt>
   * 
   * @param decl
   *          a method or constructor declaration.
   * @return a string representation of the method or constructor declaration
   *         with no return type.
   */
  public static String getSimpleSignature(IDeclFunction decl) {
    return decl.getName() + toStringParametersHelper(decl.getParameters(), false, false, false);
  }

  /**
   * Gets a string representation of the parameter list for the passed method or
   * constructor deceleration.
   * <p>
   * Examples: <tt>(java.lang.Object, java.lang.String)</tt>, <tt>()</tt>
   * 
   * @param decl
   *          a method or constructor declaration.
   * @return a string representation of the parameter list for the passed
   *         declaration.
   */
  public static String getParametersFullyQualified(IDeclFunction decl) {
    return toStringParametersHelper(decl.getParameters(), true, false, false);
  }

  private static String toStringParametersHelper(List<IDeclParameter> parameters, boolean useFullyQualifiedTypes,
      boolean showFormalNames, boolean showFinal) {
    final StringBuilder b = new StringBuilder();
    if (!parameters.isEmpty()) {
      b.append('(');
      boolean first = true;
      for (IDeclParameter parameter : parameters) {
        if (first)
          first = false;
        else
          b.append(", ");
        b.append(toStringParameterHelper(parameter, useFullyQualifiedTypes, showFormalNames, showFinal));
      }
      b.append(')');
    }
    return b.toString();
  }

  private static String toStringParameterHelper(IDeclParameter parameter, boolean useFullyQualifiedType, boolean showFormalName,
      boolean showFinal) {
    final StringBuilder b = new StringBuilder();
    if (parameter.isFinal() && showFinal)
      b.append("final ");
    if (useFullyQualifiedType)
      b.append(parameter.getTypeOf().getFullyQualified());
    else
      b.append(parameter.getTypeOf().getCompact());
    if (showFormalName) {
      b.append(' ');
      b.append(parameter.getName());
    }
    return b.toString();
  }

  private static String toStringTypeParametersHelper(List<IDeclTypeParameter> typeParameters) {
    final StringBuilder b = new StringBuilder();
    if (!typeParameters.isEmpty()) {
      b.append('<');
      boolean first = true;
      for (IDeclTypeParameter typeParameter : typeParameters) {
        if (first)
          first = false;
        else
          b.append(", ");
        b.append(toStringTypeParameterHelper(typeParameter));
      }
      b.append('>');
    }
    return b.toString();
  }

  private static String toStringTypeParameterHelper(IDeclTypeParameter typeParameter) {
    final StringBuilder b = new StringBuilder();
    b.append(typeParameter.getName());
    final List<TypeRef> bounds = typeParameter.getBounds();
    if (!bounds.isEmpty()) {
      b.append(" extends ");
      boolean first = true;
      for (TypeRef tr : bounds) {
        if (first)
          first = false;
        else
          b.append(" & ");
        b.append(tr.getCompact());
      }
    }
    return b.toString();
  }
}
