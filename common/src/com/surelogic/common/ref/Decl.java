package com.surelogic.common.ref;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

@Immutable
public abstract class Decl implements IDecl {

  static final IDecl[] EMPTY = new IDecl[0];

  /**
   * Constructs class {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ClassBuilder extends DeclBuilderVisibility {

    List<IDecl> f_formalTypeParameters = new ArrayList<IDecl>();
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
     *          the simple class name, such as <tt>ClassBuilder</tt>. This
     *          argument is ignored if the class is anonymous.
     */
    public ClassBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public ClassBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
     * Adds a formal type parameter to the end of the list of formal parameter
     * types for this declaration.
     * 
     * @param value
     *          a formal parameter type. Ignored if {@code null}.
     * @return this builder.
     */
    public ClassBuilder addFormalTypeParameter(IDecl value) {
      if (value != null)
        f_formalTypeParameters.add(value);
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

    @Override
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

      final IDecl[] formalTypeParameters = f_formalTypeParameters.isEmpty() ? EMPTY : f_formalTypeParameters
          .toArray(new IDecl[f_formalTypeParameters.size()]);
      return new DeclClass(parent, f_name, f_visibility, formalTypeParameters, f_isStatic, f_isFinal, f_isAbstract);
    }
  }

  /**
   * Constructs constructor {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ConstructorBuilder extends DeclBuilderVisibility {

    List<TypeRef> f_parameterTypes = new ArrayList<TypeRef>();

    /**
     * Constructs a constructor builder.
     * <p>
     * By default the constructor has no arguments and is <tt>public</tt>.
     */
    public ConstructorBuilder() {
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public ConstructorBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public ConstructorBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    /**
     * Adds a formal parameter type to the end of the list of formal parameter
     * types for this declaration.
     * 
     * @param value
     *          a formal parameter type. Ignored if {@code null}.
     * @return this builder.
     */
    public ConstructorBuilder addFormalParameterType(TypeRef value) {
      if (value != null)
        f_parameterTypes.add(value);
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();

      final TypeRef[] parameterTypes = f_parameterTypes.isEmpty() ? TypeRef.EMPTY : f_parameterTypes
          .toArray(new TypeRef[f_parameterTypes.size()]);
      return new DeclConstructor(parent, f_visibility, parameterTypes);
    }
  }

  /**
   * Constructs enum {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class EnumBuilder extends DeclBuilderVisibility {

    /**
     * Constructs an enum builder.
     * <p>
     * If no parent is set this enum is placed in the default package.
     * <p>
     * By default the enum is <tt>public</tt>.
     * 
     * @param name
     *          the simple enum name, such as <tt>Shapes</tt>.
     */
    public EnumBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public EnumBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public EnumBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    @Override
    public IDecl build() {
      final IDecl parent;
      // if no parent just put into the default package
      if (f_parent == null)
        parent = new PackageBuilder(null).build();
      else
        parent = f_parent.build();

      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      return new DeclEnum(parent, f_name, f_visibility);
    }
  }

  /**
   * Constructs field {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class FieldBuilder extends DeclBuilderVisibility {

    TypeRef f_typeOf;
    boolean f_isStatic = false;
    boolean f_isFinal = false;

    /**
     * Constructs a field builder.
     * <p>
     * By default the field is <tt>public</tt>, not <tt>static</tt>, and not
     * <tt>final</tt>.
     * 
     * @param name
     *          the field name.
     */
    public FieldBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public FieldBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public FieldBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    /**
     * Sets the type of this declaration.
     * 
     * @param value
     *          the type of this declaration.
     * @return this builder.
     */
    public FieldBuilder setTypeOf(TypeRef value) {
      f_typeOf = value;
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
    public FieldBuilder setIsStatic(boolean value) {
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
    public FieldBuilder setIsFinal(boolean value) {
      f_isFinal = value;
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();
      if (f_typeOf == null)
        throw new IllegalArgumentException(I18N.err(44, "typeOf"));
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      return new DeclField(parent, f_name, f_visibility, f_typeOf, f_isStatic, f_isFinal);
    }
  }

  /**
   * Constructs initializer {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class InitializerBuilder extends DeclBuilder {

    boolean f_isStatic = false;

    /**
     * Constructs an initializer builder.
     * <p>
     * By default the initializer is not <tt>static</tt>.
     */
    public InitializerBuilder() {
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public InitializerBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public InitializerBuilder setIsStatic(boolean value) {
      f_isStatic = value;
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();

      return new DeclInitializer(parent, f_isStatic);
    }
  }

  /**
   * Constructs interface {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class InterfaceBuilder extends DeclBuilderVisibility {

    List<IDecl> f_formalTypeParameters = new ArrayList<IDecl>();

    /**
     * Constructs an interface builder.
     * <p>
     * If no parent is set this interface is placed in the default package.
     * <p>
     * By default the interface is <tt>public</tt>.
     * 
     * @param name
     *          the simple interface name, such as <tt>IWork</tt>.
     */
    public InterfaceBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public InterfaceBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public InterfaceBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    /**
     * Adds a formal type parameter to the end of the list of formal parameter
     * types for this declaration.
     * 
     * @param value
     *          a formal parameter type. Ignored if {@code null}.
     * @return this builder.
     */
    public InterfaceBuilder addFormalTypeParameter(IDecl value) {
      if (value != null)
        f_formalTypeParameters.add(value);
      return this;
    }

    @Override
    public IDecl build() {
      final IDecl parent;
      // if no parent just put into the default package
      if (f_parent == null)
        parent = new PackageBuilder(null).build();
      else
        parent = f_parent.build();

      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      final IDecl[] formalTypeParameters = f_formalTypeParameters.isEmpty() ? EMPTY : f_formalTypeParameters
          .toArray(new IDecl[f_formalTypeParameters.size()]);
      return new DeclInterface(parent, f_name, f_visibility, formalTypeParameters);
    }
  }

  /**
   * Constructs method {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class MethodBuilder extends DeclBuilderVisibility {

    TypeRef f_returnTypeOf;
    List<IDecl> f_formalTypeParameters = new ArrayList<IDecl>();
    boolean f_isStatic = false;
    boolean f_isFinal = false;
    boolean f_isAbstract = false;
    List<TypeRef> f_parameterTypes = new ArrayList<TypeRef>();

    /**
     * Constructs a method builder.
     * <p>
     * By default the method has no arguments. It is <tt>public</tt>, not
     * <tt>static</tt>, not <tt>final</tt>, and not <tt>abstract</tt>.
     * 
     * @param name
     *          the method name.
     */
    public MethodBuilder(String name) {
      f_name = name;
      f_visibility = Visibility.PUBLIC;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public MethodBuilder setParent(DeclBuilder value) {
      super.setParent(value);
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
    public MethodBuilder setVisibility(Visibility value) {
      f_visibility = value == null ? Visibility.PUBLIC : value;
      return this;
    }

    /**
     * Sets the return type of this method, a value of {@code null} indicates
     * <tt>void</tt>.
     * 
     * @param value
     *          the return type of this method, a value of {@code null}
     *          indicates <tt>void</tt>.
     * @return this builder.
     * @throws IllegalArgumentException
     *           if the passed declaration is non-{@code null} and not a
     *           <tt>class</tt>, <tt>enum</tt>, or <tt>interface</tt>.
     */
    public MethodBuilder setReturnTypeOf(TypeRef value) {
      f_returnTypeOf = value;
      return this;
    }

    /**
     * Adds a formal type parameter to the end of the list of formal parameter
     * types for this declaration.
     * 
     * @param value
     *          a formal parameter type. Ignored if {@code null}.
     * @return this builder.
     */
    public MethodBuilder addFormalTypeParameter(IDecl value) {
      if (value != null)
        f_formalTypeParameters.add(value);
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
    public MethodBuilder setIsStatic(boolean value) {
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
    public MethodBuilder setIsFinal(boolean value) {
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
    public MethodBuilder setIsAbstract(boolean value) {
      f_isAbstract = value;
      return this;
    }

    /**
     * Adds a formal parameter type to the end of the list of formal parameter
     * types for this declaration.
     * 
     * @param value
     *          a formal parameter type. Ignored if {@code null}.
     * @return this builder.
     */
    public MethodBuilder addFormalParameterType(TypeRef value) {
      if (value != null)
        f_parameterTypes.add(value);
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();

      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(269, f_name));

      if (f_isAbstract && f_isStatic)
        throw new IllegalArgumentException(I18N.err(270, f_name));

      final TypeRef[] parameterTypes = f_parameterTypes.isEmpty() ? TypeRef.EMPTY : f_parameterTypes
          .toArray(new TypeRef[f_parameterTypes.size()]);
      final IDecl[] formalTypeParameters = f_formalTypeParameters.isEmpty() ? EMPTY : f_formalTypeParameters
          .toArray(new IDecl[f_formalTypeParameters.size()]);
      return new DeclMethod(parent, f_name, f_visibility, parameterTypes, f_returnTypeOf, formalTypeParameters, f_isStatic,
          f_isFinal, f_isAbstract);
    }
  }

  /**
   * Constructs package {@link IDecl} instances.
   */
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
    @Override
    public PackageBuilder setParent(DeclBuilder value) {
      super.setParent(value);
      return this;
    }

    @Override
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

  /**
   * Constructs parameter {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ParameterBuilder extends DeclBuilder {

    int f_argumentNumber;
    TypeRef f_typeOf;
    boolean f_isFinal;

    /**
     * Constructs a parameter builder.
     * 
     * @param argumentNumber
     *          the zero-based argument number of this parameter.
     */
    public ParameterBuilder(int argumentNumber) {
      f_argumentNumber = argumentNumber;
    }

    /**
     * Constructs a parameter builder.
     * 
     * @param argumentNumber
     *          the zero-based argument number of this parameter.
     * @param name
     *          the formal parameter name (this is optional, use
     *          {@link Decl.ParameterBuilder#ParameterBuilder(int)} if it is
     *          unknown).
     */
    public ParameterBuilder(int argumentNumber, String name) {
      f_argumentNumber = argumentNumber;
      f_name = name;
    }

    /**
     * Sets the parent constructor or method of this parameter.
     * 
     * @param value
     *          the parent constructor or method of this parameter.
     * @return this builder.
     */
    @Override
    public ParameterBuilder setParent(DeclBuilder value) {
      super.setParent(value);
      return this;
    }

    /**
     * Sets the type of this declaration.
     * 
     * @param value
     *          the type of this declaration.
     * @return this builder.
     */
    public ParameterBuilder setTypeOf(TypeRef value) {
      f_typeOf = value;
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
    public ParameterBuilder setIsFinal(boolean value) {
      f_isFinal = value;
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();
      if (f_typeOf == null)
        throw new IllegalArgumentException(I18N.err(44, "typeOf"));
      // see http://www.javaspecialists.eu/archive/Issue059.html
      if (f_argumentNumber < 0 || f_argumentNumber > 254)
        throw new IllegalArgumentException(I18N.err(272, f_argumentNumber));
      if (f_name == null || "".equals(f_name)) {
        f_name = "arg" + f_argumentNumber;
      } else {
        if (!SLUtility.isValidJavaIdentifier(f_name))
          throw new IllegalArgumentException(I18N.err(265, f_name));
      }
      if (!(parent.getKind() == Kind.CONSTRUCTOR || parent.getKind() == Kind.METHOD))
        throw new IllegalArgumentException(I18N.err(267, f_name, parent.getKind()));

      return new DeclParameter(parent, f_name, f_argumentNumber, f_typeOf, f_isFinal);
    }
  }

  /**
   * Constructs type parameter {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class TypeParameterBuilder extends DeclBuilder {

    List<TypeRef> f_bounds = new ArrayList<TypeRef>();
    boolean f_isFinal;

    /**
     * Constructs a type parameter builder.
     * 
     * @param name
     *          the name of this type parameter.
     */
    public TypeParameterBuilder(String name) {
      f_name = name;
    }

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    @Override
    public TypeParameterBuilder setParent(DeclBuilder value) {
      super.setParent(value);
      return this;
    }

    /**
     * Adds a type bounds to the end of the list of type bounds for this
     * declaration.
     * 
     * @param value
     *          a type bounds. Ignored if {@code null}.
     * @return this builder.
     */
    public TypeParameterBuilder addBounds(TypeRef value) {
      if (value != null)
        f_bounds.add(value);
      return this;
    }

    @Override
    public IDecl build() {
      if (f_parent == null)
        throw new IllegalArgumentException(I18N.err(44, "parent"));
      final IDecl parent = f_parent.build();

      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      final TypeRef[] bounds = f_bounds.isEmpty() ? TypeRef.EMPTY : f_bounds.toArray(new TypeRef[f_bounds.size()]);

      return new DeclTypeParameter(parent, f_name, bounds);
    }
  }

  private static abstract class DeclBuilderVisibility extends DeclBuilder {
    Visibility f_visibility;
  }

  public static abstract class DeclBuilder {

    DeclBuilder f_parent;
    String f_name;

    /**
     * Sets the parent of this declaration.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    public DeclBuilder setParent(DeclBuilder value) {
      f_parent = value;
      return this;
    }

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
  public TypeRef getTypeOf() {
    return null;
  }

  @NonNull
  public IDecl[] getFormalTypeParameters() {
    return EMPTY;
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
  public TypeRef[] getFormalParameterTypes() {
    return TypeRef.EMPTY;
  }

  public int getArgumentNumber() {
    return -1;
  }

  @Override
  public String toString() {
    LinkedList<Decl> stack = new LinkedList<Decl>();
    Decl pushMe = this;
    while (pushMe != null) {
      stack.addFirst(pushMe);
      pushMe = (Decl) pushMe.getParent();
    }
    final StringBuilder b = new StringBuilder();
    for (final Decl decl : stack) {
      b.append(decl.toStringHelper());
    }
    return b.toString();
  }

  abstract String toStringHelper();
}
