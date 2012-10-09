package com.surelogic.common.ref;

import java.util.EnumSet;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.Utility;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

@Utility
public final class DeclUtil {

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"."</tt>. If the resulting package is the
   * default package the string defined by
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return the package that the declaration is within.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null or if the declaration tree does not
   *           contain a package.
   */
  @NonNull
  public static String getPackageName(@NonNull final IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl pkgDecl = getFirstAncestorOfKind(IDecl.Kind.PACKAGE, decl);
    if (pkgDecl == null)
      throw new IllegalArgumentException(I18N.err(275, decl));
    String pkgName = pkgDecl.getName();
    if (SLUtility.JAVA_DEFAULT_PACKAGE.equals(pkgName))
      return pkgName;
    final StringBuilder b = new StringBuilder(pkgDecl.getName());
    while (true) {
      pkgDecl = pkgDecl.getParent();
      if (pkgDecl == null)
        break;
      pkgName = pkgDecl.getName();
      b.insert(0, pkgName + ".");
    }
    return b.toString();
  }

  /**
   * Gets the Java package name of the passed declaration&mdash;nested package
   * names are separated by a <tt>"."</tt>. If the resulting package is the
   * default package, or no package, then {@code null} is returned.
   * <p>
   * Example: <tt>java.util.concurrent.locks</tt>
   * 
   * @return the package that the declaration is within, or {@code null} for the
   *         default package.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null or if the declaration tree does not
   *           contain a package.
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
   * names are separated by a <tt>"/"</tt>. If the resulting package is the
   * default package, or no package, the empty string is returned.
   * <p>
   * Example: <tt>java/util/concurrent/locks</tt>
   * 
   * @return the package that the declaration is within, or <tt>""</tt> for the
   *         default package.
   */
  @NonNull
  public static String getPackageNameSlash(@NonNull final IDecl decl) {
    final String name = getPackageNameOrNull(decl);
    return name == null ? "" : name.replaceAll("\\.", "/");
  }

  /**
   * Gets the Java type name of the passed declaration&mdash;nested types are
   * separated by a <tt>"."</tt>. If the passed declaration is just for a
   * package then {@code null} is returned.
   * <p>
   * This method does not try to build a name for anonymous types or named types
   * declared within control flow. It starts at the first <tt>class</tt>,
   * <tt>enum</tt>, or <tt>interface</tt> declaration that has only parents of
   * those kinds or are a package.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map.Entry</tt>,
   * <tt>AbstractQueuedSynchronizer.ConditionObject</tt>, <tt>package-info</tt>
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
    final EnumSet<IDecl.Kind> pkgTypeKinds = EnumSet
        .of(IDecl.Kind.CLASS, IDecl.Kind.ENUM, IDecl.Kind.INTERFACE, IDecl.Kind.PACKAGE);
    IDecl typeDecl = getFirstAncestorIn(IDecl.TYPE_KINDS, decl);
    if (typeDecl == null)
      return null; // must be a package declaration
    while (!getAreAllAncestorsIn(pkgTypeKinds, typeDecl)) {
      typeDecl = getFirstAncestorIn(IDecl.TYPE_KINDS, typeDecl.getParent());
    }
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
   * separated by a <tt>"$"</tt>. If the passed declaration is just for a
   * package then {@code null} is returned.
   * <p>
   * This method does not try to build a name for anonymous types or named types
   * declared within control flow. It starts at the first <tt>class</tt>,
   * <tt>enum</tt>, or <tt>interface</tt> declaration that has only parents of
   * those kinds or are a package.
   * <p>
   * Examples: <tt>Object</tt>, <tt>/Map$Entry</tt>,
   * <tt>AbstractQueuedSynchronizer$ConditionObject</tt>, <tt>package-info</tt>
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
}
