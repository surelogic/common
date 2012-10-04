package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

@Immutable
public abstract class Decl implements IDecl {

  @NotThreadSafe
  public static final class ClassBuilder extends DeclBuilderVisibility {

    String f_formalTypeParameters;
    boolean f_isStatic = false;
    boolean f_isFinal = false;
    boolean f_isAbstract = false;

    /**
     * Constructs a class builder.
     * <p>
     * If no parent is set this class is placed in the default package.
     * <p>
     * By default the class is <tt>public</tt>, not <tt>static</tt>, not
     * <tt>final</tt>, and not <tt>abstract</tt>.
     * <p>
     * If this is an anonymous class set the visibility to
     * {@link Visibility#ANONYMOUS} (in this case the class is never
     * <tt>static</tt>).
     * <p>
     * Top-level classes cannot be <tt>static</tt>.
     * 
     * @param name
     *          the simple class name, such as <tt>ClassBuilder</tt>.
     */
    public ClassBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this class.
     * 
     * @param value
     *          the parent package of this class.
     * @return this builder.
     */
    public ClassBuilder setParent(DeclBuilder value) {
      f_parent = value;
      return this;
    }

    /**
     * Sets the visibility of this declaration.
     * 
     * @param value
     *          the visibility. If {@code null} then the visibility is changed
     *          to {@link Visibility#PUBLIC}.
     * @return this builder.
     */
    public ClassBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    /**
     * Sets the formal type parameters, such as <tt>&lt;E&gt;</tt> for
     * <tt>List&lt;E&gt;</tt>, for this class.
     * 
     * @param value
     *          the formal type parameters.
     * @return this builder.
     */
    public ClassBuilder setFormalTypeParameters(String value) {
      f_formalTypeParameters = value;
      return this;
    }

    /**
     * Sets if this declaration is declared to be <i>static</i>.
     * 
     * @param value
     *          {@code true} if this declaration is declared to be
     *          <i>static</i>, {@code false} otherwise.
     * @return this builder.
     */
    public ClassBuilder setIsStatic(boolean value) {
      f_isStatic = value;
      return this;
    }

    /**
     * Sets if this declaration is declared to be <i>final</i>.
     * 
     * @param value
     *          {@code true} if this declaration is declared to be <i>final</i>,
     *          {@code false} otherwise.
     * @return this builder.
     */
    public ClassBuilder setIsFinal(boolean value) {
      f_isFinal = value;
      return this;
    }

    /**
     * Sets if this declaration is declared to be <i>abstract</i>.
     * 
     * @param value
     *          {@code true} if this declaration is declared to be
     *          <i>abstract</i>, {@code false} otherwise.
     * @return this builder.
     */
    public ClassBuilder setIsAbstract(boolean value) {
      f_isAbstract = value;
      return this;
    }

    public IDecl build() {
      final IDecl parent;
      // if no parent just put into the default package
      if (f_parent == null)
        parent = new PackageBuilder(null).build();
      else
        parent = f_parent.build();

      // anonymous classes
      if (f_visibility == Visibility.ANONYMOUS) {
        f_isStatic = false;
        f_name = "";
      } else {
        if (!SLUtility.isValidJavaIdentifier(f_name))
          throw new IllegalArgumentException(I18N.err(265, f_name));
      }

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      // a top-level class can never be static
      if (parent.getKind() == Kind.PACKAGE)
        f_isStatic = false;

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(266, f_name));

      return new DeclClass(parent, f_name, f_visibility, f_formalTypeParameters, f_isStatic, f_isFinal, f_isAbstract);
    }
  }

  @NotThreadSafe
  public static final class PackageBuilder extends DeclBuilder {

    /**
     * Constructs a package builder.
     * 
     * @param name
     *          the package name. <tt>""</tt> or <tt>null</tt> indicate the
     *          default package. The name can be simple, such as <tt>com</tt>,
     *          or nested, such as <tt>com.surelogic.work</tt>.
     */
    public PackageBuilder(String name) {
      f_name = name;
    }

    /**
     * Sets the parent package of this package.
     * 
     * @param value
     *          the parent package of this package.
     * @return this builder.
     */
    public PackageBuilder setParent(PackageBuilder value) {
      f_parent = value;
      return this;
    }

    public IDecl build() {
      if (f_name == null || "".equals(f_name)) {
        /*
         * Default package
         */
        if (f_parent == null) {
          return new DeclPackage(); // default package
        } else
          throw new IllegalArgumentException(I18N.err(264));
      }

      final StringBuilder b = new StringBuilder(f_name);
      IDecl parent = f_parent == null ? null : f_parent.build();
      if (parent != null && parent.getName().equals(SLUtility.JAVA_DEFAULT_PACKAGE)) {
        /*
         * If our parent is the default package, rather than throw an error we
         * just ignore this problem and act as if we have no parent.
         */
        parent = null;
      }
      boolean hasDot = true;
      while (hasDot) {
        final int dotIndex = b.indexOf(".");
        hasDot = dotIndex != -1;
        final String name = hasDot ? b.substring(0, dotIndex) : b.toString();
        if (name.length() > 0) {
          if (SLUtility.isValidJavaIdentifier(name))
            parent = new DeclPackage(parent, name);
          else
            throw new IllegalArgumentException(I18N.err(262, name, f_name));
        } else
          throw new IllegalArgumentException(I18N.err(263, f_name));
        if (hasDot)
          b.delete(0, dotIndex + 1);
      }
      return parent;
    }
  }

  private static abstract class DeclBuilderVisibility extends DeclBuilder {
    Visibility f_visibility;
  }

  private static abstract class DeclBuilder {

    DeclBuilder f_parent;
    String f_name;

    /**
     * Builds an appropriate {@link IDecl} instance. Throws an exception if
     * something goes wrong.
     * 
     * @return a new {@link IDecl} instance.
     * @throws IllegalArgumentException
     *           if something goes wrong.
     */
    public abstract IDecl build();
  }

  static final IDecl[] EMPTY = new IDecl[0];

  @Nullable
  final IDecl f_parent;
  @NonNull
  final String f_name;

  public Decl(IDecl parent, String name) {
    f_parent = parent;
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    f_name = name;
  }

  @Nullable
  public final IDecl getParent() {
    return f_parent;
  }

  @NonNull
  public final String getName() {
    return f_name;
  }

  @Nullable
  public IDecl getTypeOf() {
    return null;
  }

  @NonNull
  public String getFormalTypeParameters() {
    return "";
  }

  @NonNull
  public Visibility getVisiblity() {
    return Visibility.NA;
  }

  public boolean isStatic() {
    return false;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isAbstract() {
    return false;
  }

  @NonNull
  public IDecl[] getFormalParameters() {
    return EMPTY;
  }
}
