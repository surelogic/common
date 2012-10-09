package com.surelogic.common.ref;

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
   * @return The package that the declaration is within.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is null or if the declaration tree does not
   *           contain a package.
   */
  @NonNull
  public static String getPackageName(@NonNull final IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl pkgDecl = getParentOfKind(decl, IDecl.Kind.PACKAGE);
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
   * @return The package that the declaration is within, or {@code null} for the
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
   * @return The package that the declaration is within, or <tt>""</tt> for the
   *         default package.
   */
  @NonNull
  public static String getPackageNameSlash(@NonNull final IDecl decl) {
    final String name = getPackageNameOrNull(decl);
    return name == null ? "" : name.replaceAll("\\.", "/");
  }

  @Nullable
  public static String getTypeNameOrNull(@NonNull final IDecl decl) {
    return null;
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
   * Returns the first enclosing parent declaration of that matches the passed
   * kind. If the passed declaration matches it is returned.
   * 
   * @param decl
   *          any declaration.
   * @param kind
   *          a kind of declaration.
   * @return the first parent declaration of the passed declaration that is of
   *         the passed kind, or {@code null} if none can be found.
   */
  @Nullable
  public static IDecl getParentOfKind(@NonNull final IDecl decl, @NonNull final IDecl.Kind kind) {
    if (decl == null || kind == null)
      return null;
    IDecl node = decl;
    while (true) {
      if (node.getKind() == kind)
        return node;
      node = node.getParent();
      if (node == null)
        return null;
    }
  }
}
