package com.surelogic.common.ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.common.Pair;
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;
import com.surelogic.common.xml.XMLUtil;

@Immutable
public abstract class Decl implements IDecl {

  /**
   * Constructs class {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ClassBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;
    boolean f_isStatic = false;
    boolean f_isFinal = false;
    boolean f_isAbstract = false;

    /**
     * Constructs a class builder.
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
     * Adds a type parameter to this declaration. This is a convenience method
     * that is the same as setting the parent of the
     * {@link TypeParameterBuilder} to this, i.e.,
     * <tt>o.addTypeParameter(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public ClassBuilder addTypeParameter(TypeParameterBuilder value) {
      if (value != null)
        value.setParent(this);
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
    public IDecl buildInternal(IDecl parent) {
      // anonymous classes
      if (f_visibility == Visibility.ANONYMOUS) {
        f_isStatic = false;
        f_name = "";
      } else {
        if (!SLUtility.isValidJavaIdentifier(f_name))
          throw new IllegalArgumentException(I18N.err(275, f_name));
      }

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      // a top-level class can never be static
      if (parent.getKind() == Kind.PACKAGE)
        f_isStatic = false;

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(276, f_name));

      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(284, position));
          usedTypeParmPositions.add(position);
        }
      }

      return new DeclClass(parent, f_childBuilders, f_name, f_visibility, f_isStatic, f_isFinal, f_isAbstract);
    }
  }

  /**
   * Constructs constructor {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ConstructorBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;

    /**
     * Constructs a constructor builder.
     * <p>
     * By default the constructor has no arguments and is <tt>public</tt>.
     */
    public ConstructorBuilder() {
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
     * Adds a parameter to this declaration. This is a convenience method that
     * is the same as setting the parent of the {@link ParameterBuilder} to
     * this, i.e., <tt>o.addParameterType(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public ConstructorBuilder addParameter(ParameterBuilder value) {
      if (value != null)
        value.setParent(this);
      return this;
    }

    /**
     * Adds a type parameter to this declaration. This is a convenience method
     * that is the same as setting the parent of the
     * {@link TypeParameterBuilder} to this, i.e.,
     * <tt>o.addTypeParameter(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public ConstructorBuilder addTypeParameter(TypeParameterBuilder value) {
      if (value != null)
        value.setParent(this);
      return this;
    }

    @Override
    public IDecl buildInternal(IDecl parent) {
      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      final Set<Integer> usedParmPositions = new HashSet<Integer>();
      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof ParameterBuilder) {
          final Integer position = Integer.valueOf(((ParameterBuilder) b).f_position);
          if (usedParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(283, position));
          usedParmPositions.add(position);
        } else if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(284, position));
          usedTypeParmPositions.add(position);
        }
      }

      return new DeclConstructor(parent, f_childBuilders, f_visibility);
    }
  }

  /**
   * Constructs enum {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class EnumBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;

    /**
     * Constructs an enum builder.
     * <p>
     * By default the enum is <tt>public</tt>.
     * 
     * @param name
     *          the simple enum name, such as <tt>Shapes</tt>.
     */
    public EnumBuilder(String name) {
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
    public IDecl buildInternal(IDecl parent) {
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(275, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      return new DeclEnum(parent, f_childBuilders, f_name, f_visibility);
    }
  }

  /**
   * Constructs field {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class FieldBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;
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
    public IDecl buildInternal(IDecl parent) {
      if (f_typeOf == null)
        throw new IllegalArgumentException(I18N.err(44, "typeOf"));
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(275, f_name));
      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      return new DeclField(parent, f_childBuilders, f_name, f_visibility, f_typeOf, f_isStatic, f_isFinal);
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
    public IDecl buildInternal(IDecl parent) {
      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      return new DeclInitializer(parent, f_childBuilders, f_isStatic);
    }
  }

  /**
   * Constructs interface {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class InterfaceBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;

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
     * Adds a type parameter to this declaration. This is a convenience method
     * that is the same as setting the parent of the
     * {@link TypeParameterBuilder} to this, i.e.,
     * <tt>o.addTypeParameter(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public InterfaceBuilder addTypeParameter(TypeParameterBuilder value) {
      if (value != null)
        value.setParent(this);
      return this;
    }

    @Override
    public IDecl buildInternal(IDecl parent) {
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(275, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(284, position));
          usedTypeParmPositions.add(position);
        }
      }

      return new DeclInterface(parent, f_childBuilders, f_name, f_visibility);
    }
  }

  /**
   * Constructs method {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class MethodBuilder extends DeclBuilder {

    Visibility f_visibility = Visibility.PUBLIC;
    TypeRef f_returnTypeOf;
    boolean f_isStatic = false;
    boolean f_isFinal = false;
    boolean f_isAbstract = false;

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
     * Adds a type parameter to this declaration. This is a convenience method
     * that is the same as setting the parent of the
     * {@link TypeParameterBuilder} to this, i.e.,
     * <tt>o.addTypeParameter(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public MethodBuilder addTypeParameter(TypeParameterBuilder value) {
      if (value != null)
        value.setParent(this);
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
     * Adds a parameter to this declaration. This is a convenience method that
     * is the same as setting the parent of the {@link ParameterBuilder} to
     * this, i.e., <tt>o.addParameterType(p)</tt> has the same effect as
     * <tt>p.setParent(o)</tt>.
     * 
     * @param value
     *          a type parameter builder. Ignored if {@code null}.
     * @return this builder.
     */
    public MethodBuilder addParameter(ParameterBuilder value) {
      if (value != null)
        value.setParent(this);
      return this;
    }

    @Override
    public IDecl buildInternal(IDecl parent) {
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(275, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(279, f_name));

      if (f_isAbstract && f_isStatic)
        throw new IllegalArgumentException(I18N.err(280, f_name));

      final Set<Integer> usedParmPositions = new HashSet<Integer>();
      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof ParameterBuilder) {
          final Integer position = Integer.valueOf(((ParameterBuilder) b).f_position);
          if (usedParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(283, position));
          usedParmPositions.add(position);
        } else if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(284, position));
          usedTypeParmPositions.add(position);
        }
      }

      return new DeclMethod(parent, f_childBuilders, f_name, f_visibility, f_returnTypeOf, f_isStatic, f_isFinal, f_isAbstract);
    }
  }

  /**
   * Constructs package {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class PackageBuilder extends DeclBuilder {

    /**
     * Constructs a package builder for the default package.
     */
    public PackageBuilder() {
      f_name = null;
    }

    /**
     * Constructs a package builder.
     * 
     * @param name
     *          the complete package name, such as <tt>org.apache</tt> or
     *          <tt>com.surelogic.dropsea</tt>. <tt>""</tt> or <tt>null</tt>
     *          indicate the default package.
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
    IDecl buildInternal(IDecl parent) {
      if (parent != null)
        throw new IllegalArgumentException(I18N.err(274, f_name, parent));

      if (f_name == null || "".equals(f_name)) {
        f_name = SLUtility.JAVA_DEFAULT_PACKAGE;
      } else {
        if (!SLUtility.isValidDotSeparatedJavaIdentifier(f_name))
          throw new IllegalArgumentException(I18N.err(275, f_name));
      }
      return new DeclPackage(f_childBuilders, f_name);
    }
  }

  /**
   * Constructs parameter {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ParameterBuilder extends DeclBuilder {

    int f_position;
    TypeRef f_typeOf;
    boolean f_isFinal = false;

    /**
     * Constructs a parameter builder.
     * <p>
     * By default this parameter is not <tt>final</tt>.
     * 
     * @param position
     *          the zero-based argument number of this parameter.
     */
    public ParameterBuilder(int position) {
      f_position = position;
    }

    /**
     * Constructs a parameter builder.
     * <p>
     * By default this parameter is not <tt>final</tt>.
     * 
     * @param position
     *          the zero-based argument number of this parameter.
     * @param name
     *          the formal parameter name (this is optional, use
     *          {@link Decl.ParameterBuilder#ParameterBuilder(int)} if it is
     *          unknown).
     */
    public ParameterBuilder(int position, String name) {
      f_position = position;
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
    public IDecl buildInternal(IDecl parent) {
      if (f_typeOf == null)
        throw new IllegalArgumentException(I18N.err(44, "typeOf"));

      if (f_name == null || "".equals(f_name)) {
        f_name = "arg" + f_position;
      } else {
        if (!SLUtility.isValidJavaIdentifier(f_name))
          throw new IllegalArgumentException(I18N.err(275, f_name));
      }

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      final Kind parentKind = parent.getKind();
      if (!(parentKind == Kind.CONSTRUCTOR || parentKind == Kind.METHOD))
        throw new IllegalArgumentException(I18N.err(277, f_name, parentKind));

      return new DeclParameter(parent, f_childBuilders, f_name, f_position, f_typeOf, f_isFinal);
    }
  }

  /**
   * Constructs type parameter {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class TypeParameterBuilder extends DeclBuilder {

    int f_position;
    List<TypeRef> f_bounds = new ArrayList<TypeRef>();

    /**
     * Constructs a type parameter builder.
     * 
     * @param position
     *          the zero-based argument number of this type parameter.
     * @param name
     *          the name of this type parameter.
     */
    public TypeParameterBuilder(int position, String name) {
      f_position = position;
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
    public IDecl buildInternal(IDecl parent) {
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(275, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(272, f_name));

      final Kind parentKind = parent.getKind();
      if (!(parentKind == Kind.CLASS || parentKind == Kind.INTERFACE || parentKind == Kind.METHOD || parentKind == Kind.CONSTRUCTOR))
        throw new IllegalArgumentException(I18N.err(273, f_name, parentKind));

      return new DeclTypeParameter(parent, f_childBuilders, f_name, f_position, f_bounds);
    }
  }

  public static abstract class DeclBuilder {

    DeclBuilder f_parent;
    final List<DeclBuilder> f_childBuilders = new ArrayList<DeclBuilder>();
    String f_name;

    /**
     * Only valid after the build.
     */
    IDecl f_declaration;

    /**
     * Gets the parameter builder at the passed position.
     * 
     * @param position
     *          the zero-based argument number of this parameter.
     * @return the parameter builder at the passed position.
     * 
     * @throws IllegalArgumentException
     *           if no parameter builder can be found at the passed position.
     */
    public ParameterBuilder getParameterBuilderAt(int position) {
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof ParameterBuilder) {
          final ParameterBuilder pb = (ParameterBuilder) b;
          if (pb.f_position == position)
            return pb;
        }
      }
      throw new IllegalArgumentException(I18N.err(286, ParameterBuilder.class.getSimpleName(), position, this.getClass()
          .getSimpleName(), f_name));
    }

    /**
     * Gets the type parameter builder at the passed position.
     * 
     * @param position
     *          the zero-based argument number of this type parameter.
     * @return the type parameter builder at the passed position.
     * 
     * @throws IllegalArgumentException
     *           if no type parameter builder can be found at the passed
     *           position.
     */
    public TypeParameterBuilder getTypeParameterBuilderAt(int position) {
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof TypeParameterBuilder) {
          final TypeParameterBuilder pb = (TypeParameterBuilder) b;
          if (pb.f_position == position)
            return pb;
        }
      }
      throw new IllegalArgumentException(I18N.err(286, TypeParameterBuilder.class.getSimpleName(), position, this.getClass()
          .getSimpleName(), f_name));
    }

    /**
     * Sets the parent of this declaration. If the parent is non-null then this
     * builder is added as one of its set of child builders.
     * <p>
     * Multiple calls with the same value are ignored.
     * 
     * @param value
     *          the parent of this declaration.
     * @return this builder.
     */
    public DeclBuilder setParent(@Nullable DeclBuilder value) {
      f_parent = value;
      if (value != null && !value.f_childBuilders.contains(this))
        value.f_childBuilders.add(this);
      return this;
    }

    /**
     * Builds an appropriate {@link IDecl} instance by finding the root builder
     * and constructing a declaration tree from the root out. Throws an
     * exception if something goes wrong.
     * 
     * @return a {@link IDecl} instance for this builder.
     * @throws IllegalArgumentException
     *           if something goes wrong.
     */
    public final IDecl build() {
      // find the root
      DeclBuilder root = this;
      while (root.f_parent != null) {
        root = root.f_parent;
      }
      // build the declaration tree
      root.buildHelper(null);
      // returned the stashed IDecl from buildHelper
      return f_declaration;
    }

    private final IDecl buildHelper(IDecl parent) {
      final IDecl stash = buildInternal(parent);
      f_declaration = stash;
      return stash;
    }

    abstract IDecl buildInternal(IDecl parent);
  }

  /**
   * {@code null} indicates at the root.
   */
  @Nullable
  final IDecl f_parent;
  /**
   * {@code null} indicates no children. Call {@link #getChildren()} instead of
   * using this field directly.
   */
  @Nullable
  final IDecl[] f_children;
  @NonNull
  final String f_name;

  /**
   * Sorts parameters and type parameters by argument position.
   */
  final Comparator<IDecl> f_byPosition = new Comparator<IDecl>() {
    public int compare(IDecl o1, IDecl o2) {
      if (o1 == null && o2 == null)
        return 0;
      if (o1 == null && o2 != null)
        return -1;
      if (o1 != null && o2 == null)
        return 1;
      return o1.getPosition() - o2.getPosition();
    }
  };

  /**
   * Constructs a declaration tree from the root out. Children are constructed
   * by this method so that a parent-to-child and child-to-parent links can be
   * set up that are immutable.
   * <p>
   * To construct children the {@link DeclBuilder#buildHelper(IDecl)} is used.
   * 
   * @param parent
   *          the parent of this declaration, {@code null} if at the root of the
   *          declaration tree.
   * @param childBuilders
   *          builders for all the children of this declaration.
   * @param name
   *          the name of this declaration.
   */
  public Decl(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name) {
    f_parent = parent;
    if (name == null)
      throw new IllegalArgumentException(I18N.err(44, "name"));
    f_name = name;
    final List<IDecl> children = new ArrayList<IDecl>();
    for (Decl.DeclBuilder childBuilder : childBuilders) {
      children.add(childBuilder.buildHelper(this));
    }
    if (children.isEmpty())
      f_children = null;
    else
      f_children = children.toArray(new IDecl[children.size()]);
  }

  @Nullable
  public final IDecl getParent() {
    return f_parent;
  }

  @NonNull
  public final List<IDecl> getChildren() {
    if (f_children == null)
      return Collections.emptyList();
    else {
      final List<IDecl> result = new ArrayList<IDecl>();
      for (IDecl decl : f_children)
        result.add(decl);
      return result;
    }
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
  public Visibility getVisibility() {
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
  public final List<IDecl> getParameters() {
    List<IDecl> work = new ArrayList<IDecl>();
    for (IDecl decl : getChildren()) {
      if (decl instanceof DeclParameter)
        work.add(decl);
    }
    Collections.sort(work, f_byPosition);
    return work;
  }

  public int getPosition() {
    return -1;
  }

  @NonNull
  public final List<IDecl> getTypeParameters() {
    List<IDecl> work = new ArrayList<IDecl>();
    for (IDecl decl : getChildren()) {
      if (decl instanceof DeclTypeParameter)
        work.add(decl);
    }
    Collections.sort(work, f_byPosition);
    return work;
  }

  @NonNull
  public List<TypeRef> getBounds() {
    return Collections.emptyList();
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

  protected static String toStringHelperTypeParameters(IDecl decl) {
    List<IDecl> params = decl.getTypeParameters();
    if (params.isEmpty())
      return "";

    final StringBuilder b = new StringBuilder("<");
    boolean first = true;
    for (IDecl param : params) {
      if (first)
        first = false;
      else
        b.append(",");
      b.append(((Decl) param).toStringHelper());
    }
    b.append(">");
    return b.toString();
  }

  protected static String toStringHelperParameters(IDecl decl) {
    List<IDecl> params = decl.getParameters();
    final StringBuilder b = new StringBuilder("(");
    boolean first = true;
    for (IDecl param : params) {
      if (first)
        first = false;
      else
        b.append(",");
      b.append(((Decl) param).toStringHelper());
    }
    b.append(")");
    return b.toString();
  }

  /**
   * Constructs and returns a declaration from the passed string if it is a
   * valid fully-qualified Java type name in a particular SureLogic format.
   * <p>
   * This method has some limitations because it works with just names. For
   * example, it has no idea if the type names in <tt>value</tt> are
   * <tt>class</tt>, <tt>enum</tt> , or <tt>interface</tt>
   * declarations&mdash;they all default to <tt>class</tt>. All other
   * declaration settings, such as visibility, also take default values.
   * 
   * @param value
   *          a valid fully-qualified Java type name in a particular SureLogic
   *          format.
   * @return a declaration.
   * @throws IllegalArgumentException
   *           if {@link #isValidTypeNameFullyQualifiedSureLogic(String)} fails
   *           for <tt>value</tt>.
   */
  public static IDecl getDeclForTypeNameFullyQualifiedSureLogic(String value) {
    final PackageBuilder pb = new PackageBuilder(SLUtility.getPackageNameOrEmptyFromTypeNameFullyQualifiedSureLogic(value));
    DeclBuilder last = pb;
    for (String typeName : SLUtility.getTypeNamesOrEmptyFromTypeNameFullyQualifiedSureLogic(value)) {
      last = new ClassBuilder(typeName).setParent(last);
    }
    return last.build();
  }

  private static final String DECL = "^v1^";
  private static final String START = DECL + "[";
  private static final String START_TO_RETURN = DECL + "->[";
  private static final String END = "]";
  private static final String SEP = "|";
  private static final String NAME = "name";
  private static final String VISIBILITY = "visibility";
  private static final String STATIC = "static";
  private static final String FINAL = "final";
  private static final String ABSTRACT = "abstract";
  private static final String TYPE = "type";
  private static final String POSITION = "position";
  private static final String BOUNDS = "bounds";

  /**
   * Encodes the passed declaration for persistence as a string. Note that this
   * string is not escaped properly for XML (see {@link XMLUtil#escape(String)}
   * to do this).
   * 
   * @param decl
   *          any declaration.
   * @return a string.
   * 
   * @throws IllegalArgumentException
   *           if <tt>decl</tt> is {@code null}.
   */
  @NonNull
  public static String encodeForPersistence(final IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    IDecl root = DeclUtil.getRoot(decl);
    final StringBuilder b = new StringBuilder();
    encodeHelper(root, b, decl);
    return b.toString();
  }

  /**
   * Recursive call to encode a declaration and its children into the passed
   * {@link StringBuilder}.
   * 
   * @param decl
   *          any declaration.
   * @param b
   *          a mutable string.
   */
  private static void encodeHelper(final IDecl decl, final StringBuilder b, final IDecl toReturn) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    if (decl == toReturn)
      b.append(START_TO_RETURN);
    else
      b.append(START);
    b.append(decl.getKind().toString());
    b.append(SEP);
    switch (decl.getKind()) {
    case CLASS:
      add(NAME, decl.getName(), b);
      addV(VISIBILITY, decl.getVisibility(), b);
      addB(STATIC, decl.isStatic(), b);
      addB(FINAL, decl.isFinal(), b);
      addB(ABSTRACT, decl.isAbstract(), b);
      break;
    case CONSTRUCTOR:
      addV(VISIBILITY, decl.getVisibility(), b);
      break;
    case ENUM:
      add(NAME, decl.getName(), b);
      addV(VISIBILITY, decl.getVisibility(), b);
      break;
    case FIELD:
      add(NAME, decl.getName(), b);
      addV(VISIBILITY, decl.getVisibility(), b);
      addB(STATIC, decl.isStatic(), b);
      addB(FINAL, decl.isFinal(), b);
      addT(TYPE, decl.getTypeOf(), b);
      break;
    case INITIALIZER:
      addB(STATIC, decl.isStatic(), b);
      break;
    case INTERFACE:
      add(NAME, decl.getName(), b);
      addV(VISIBILITY, decl.getVisibility(), b);
      break;
    case METHOD:
      add(NAME, decl.getName(), b);
      addV(VISIBILITY, decl.getVisibility(), b);
      addB(STATIC, decl.isStatic(), b);
      addB(FINAL, decl.isFinal(), b);
      addB(ABSTRACT, decl.isAbstract(), b);
      addT(TYPE, decl.getTypeOf(), b);
      break;
    case PACKAGE:
      final String pkgName = decl.getName();
      if (!pkgName.equals(SLUtility.JAVA_DEFAULT_PACKAGE))
        add(NAME, decl.getName(), b);
      break;
    case PARAMETER:
      add(POSITION, Integer.toString(decl.getPosition()), b);
      add(NAME, decl.getName(), b);
      addB(FINAL, decl.isFinal(), b);
      addT(TYPE, decl.getTypeOf(), b);
      break;
    case TYPE_PARAMETER:
      add(POSITION, Integer.toString(decl.getPosition()), b);
      add(NAME, decl.getName(), b);
      final List<TypeRef> bounds = decl.getBounds();
      if (!bounds.isEmpty())
        add(BOUNDS, TypeRef.encodeListForPersistence(decl.getBounds()), b);
      break;
    }
    for (final IDecl child : decl.getChildren()) {
      encodeHelper(child, b, toReturn);
    }
    b.append(END);
  }

  /*
   * Simple helper methods for encoding a declaration
   */

  private static void add(String name, String value, final StringBuilder b) {
    b.append(name).append('=').append(value).append(SEP);
  }

  private static void addV(String name, IDecl.Visibility value, final StringBuilder b) {
    if (value != Visibility.PUBLIC)
      b.append(name).append('=').append(value.toString()).append(SEP);
  }

  private static void addB(String name, boolean value, final StringBuilder b) {
    if (value)
      b.append(name).append('=').append(Boolean.toString(value)).append(SEP);
  }

  private static void addT(String name, TypeRef value, final StringBuilder b) {
    if (value != null)
      b.append(name).append('=').append(value.encodeForPersistence()).append(SEP);
  }

  /**
   * Parses the result of {@link #encodeForPersistence(IDecl)} back to a
   * {@link IDecl}.
   * 
   * @param value
   *          an encoded string.
   * @return a declaration.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  @NonNull
  public static IDecl parseEncodedForPersistence(final String value) {
    if (value == null)
      throw new IllegalArgumentException(I18N.err(44, "value"));
    final StringBuilder b = new StringBuilder(value.trim());
    // the builder returned is the one we should build and return
    final DeclBuilder builder = parseToBuilderHelper(null, b);
    return builder.build();
  }

  /**
   * Recursive helper method to parse an encoded declaration.
   * 
   * @param parent
   *          the parent of the encoded declaration to be parsed. {@code null}
   *          at root.
   * @param b
   *          a mutable string.
   * @return the declaration builder selected for return. The declaration
   *         encoded for return is special in that it begins with
   *         {@link #START_TO_RETURN} rather than {@link #START}.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  private static DeclBuilder parseToBuilderHelper(@Nullable final DeclBuilder parent, @NonNull final StringBuilder b) {
    boolean toReturn = false;
    if (b.toString().startsWith(START_TO_RETURN)) {
      b.delete(0, START_TO_RETURN.length());
      toReturn = true;
    } else {
      if (b.toString().startsWith(START)) {
        b.delete(0, START.length());
      } else
        throw new IllegalArgumentException("no encoded declaration found in: " + b);
    }
    final Kind kind = Kind.valueOf(toNext("|", b));
    DeclBuilder thisDeclBuilder = null;
    Pair<String, String> pair = null;
    int position;
    /*
     * Note that the order of checking for "name=value'-pairs must match the
     * order of output to work.
     */
    switch (kind) {
    case CLASS:
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("CLASS must have a name");
      final ClassBuilder classBuilder = new ClassBuilder(pair.second());
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        classBuilder.setVisibility(Visibility.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(STATIC, pair)) {
        classBuilder.setIsStatic(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(FINAL, pair)) {
        classBuilder.setIsFinal(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(ABSTRACT, pair)) {
        classBuilder.setIsAbstract(Boolean.valueOf(pair.second()));
      }
      thisDeclBuilder = classBuilder;
      break;
    case CONSTRUCTOR:
      final ConstructorBuilder constructorBuilder = new ConstructorBuilder();
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        constructorBuilder.setVisibility(Visibility.valueOf(pair.second()));
      }
      thisDeclBuilder = constructorBuilder;
      break;
    case ENUM:
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("ENUM must have a name");
      final EnumBuilder enumBuilder = new EnumBuilder(pair.second());
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        enumBuilder.setVisibility(Visibility.valueOf(pair.second()));
      }
      thisDeclBuilder = enumBuilder;
      break;
    case FIELD:
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("FIELD must have a name");
      final FieldBuilder fieldBuilder = new FieldBuilder(pair.second());
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        fieldBuilder.setVisibility(Visibility.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(STATIC, pair)) {
        fieldBuilder.setIsStatic(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(FINAL, pair)) {
        fieldBuilder.setIsFinal(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isNotFor(TYPE, pair))
        throw new IllegalArgumentException("FIELD must have a type");
      fieldBuilder.setTypeOf(TypeRef.parseEncodedForPersistence(pair.second()));
      thisDeclBuilder = fieldBuilder;
      break;
    case INITIALIZER:
      final InitializerBuilder initializerBuilder = new InitializerBuilder();
      pair = parseEqualsPair(b);
      if (isFor(STATIC, pair)) {
        initializerBuilder.setIsStatic(Boolean.valueOf(pair.second()));
      }
      thisDeclBuilder = initializerBuilder;
      break;
    case INTERFACE:
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("INTERFACE must have a name");
      final InterfaceBuilder interfaceBuilder = new InterfaceBuilder(pair.second());
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        interfaceBuilder.setVisibility(Visibility.valueOf(pair.second()));
      }
      thisDeclBuilder = interfaceBuilder;
      break;
    case METHOD:
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("METHOD must have a name");
      final MethodBuilder methodBuilder = new MethodBuilder(pair.second());
      pair = parseEqualsPair(b);
      if (isFor(VISIBILITY, pair)) {
        methodBuilder.setVisibility(Visibility.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(STATIC, pair)) {
        methodBuilder.setIsStatic(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(FINAL, pair)) {
        methodBuilder.setIsFinal(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(ABSTRACT, pair)) {
        methodBuilder.setIsAbstract(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isFor(TYPE, pair))
        methodBuilder.setReturnTypeOf(TypeRef.parseEncodedForPersistence(pair.second()));
      thisDeclBuilder = methodBuilder;
      break;
    case PACKAGE:
      pair = parseEqualsPair(b);
      if (isFor(NAME, pair))
        thisDeclBuilder = new PackageBuilder(pair.second());
      else
        thisDeclBuilder = new PackageBuilder();
      break;
    case PARAMETER:
      pair = parseEqualsPair(b);
      if (isNotFor(POSITION, pair))
        throw new IllegalArgumentException("PARAMETER must have a position");
      position = Integer.parseInt(pair.second());
      pair = parseEqualsPair(b);
      final ParameterBuilder parameterBuilder;
      if (isFor(NAME, pair)) {
        parameterBuilder = new ParameterBuilder(position, pair.second());
        pair = parseEqualsPair(b);
      } else {
        parameterBuilder = new ParameterBuilder(position);
      }
      if (isFor(FINAL, pair)) {
        parameterBuilder.setIsFinal(Boolean.valueOf(pair.second()));
        pair = parseEqualsPair(b);
      }
      if (isNotFor(TYPE, pair))
        throw new IllegalArgumentException("PARAMETER must have a type");
      parameterBuilder.setTypeOf(TypeRef.parseEncodedForPersistence(pair.second()));
      thisDeclBuilder = parameterBuilder;
      break;
    case TYPE_PARAMETER:
      pair = parseEqualsPair(b);
      if (isNotFor(POSITION, pair))
        throw new IllegalArgumentException("TYPE PARAMETER must have a position");
      position = Integer.parseInt(pair.second());
      pair = parseEqualsPair(b);
      if (isNotFor(NAME, pair))
        throw new IllegalArgumentException("TYPE PARAMETER must have a name");
      final TypeParameterBuilder typeParameterBuilder = new TypeParameterBuilder(position, pair.second());
      pair = parseEqualsPair(b);
      if (isFor(BOUNDS, pair)) {
        final List<TypeRef> bounds = TypeRef.parseListEncodedForPersistence(pair.second());
        for (TypeRef value : bounds)
          typeParameterBuilder.addBounds(value);
      }
      thisDeclBuilder = typeParameterBuilder;
      break;
    }
    if (thisDeclBuilder == null)
      throw new IllegalArgumentException("thisDeclBuilder should not be null -- code bug");
    thisDeclBuilder.setParent(parent);

    DeclBuilder toReturnBuilder = null;

    /*
     * parse children recursively
     */
    while (b.toString().startsWith(DECL)) {
      final DeclBuilder child = parseToBuilderHelper(thisDeclBuilder, b);
      if (child != null)
        toReturnBuilder = child;
    }
    if (!b.toString().startsWith(END))
      throw new IllegalArgumentException("encoded declaration should finsh up with \"]\" but does not: " + b);
    b.delete(0, END.length());

    if (toReturn)
      toReturnBuilder = thisDeclBuilder;

    return toReturnBuilder;
  }

  /**
   * Gets a name/value pair encoded as <tt>static=true|</tt> or {@code null} if
   * the passed mutable string starts with {@link #DECL} (a new declaration) or
   * {@link #END}.
   * <p>
   * The string is mutated to remove up to and including the <tt>|</tt> if a
   * non-null result is returned. it is not modified if the result is null.
   * 
   * @param b
   *          a mutable string.
   * @return a name/value pair or {@code null}.
   * 
   * @throws IllegalArgumentException
   *           if something goes wrong.
   */
  @Nullable
  private static Pair<String, String> parseEqualsPair(final StringBuilder b) {
    if (b.toString().startsWith(DECL) || b.toString().startsWith(END))
      return null;
    final String pair = toNext("|", b);
    final int index = pair.indexOf('=');
    if (index == -1)
      throw new IllegalArgumentException("Failed to find a name=value pair (e.g., static=true) in: " + pair);
    final String name = pair.substring(0, index);
    final String value = pair.substring(index + 1);
    return new Pair<String, String>(name, value);
  }

  /**
   * Checks if the name/value pair is not a match with the given name. If this
   * method returns {@code true} an exception is typically thrown.
   * 
   * @param name
   *          a name.
   * @param pair
   *          a name/value pair.
   * @return {@code true} if <tt>pair == null</tt> or <tt>name</tt> is not equal
   *         to <tt>pair.first()</tt>, {@code false} otherwise.
   */
  private static boolean isNotFor(String name, Pair<String, String> pair) {
    if (pair == null)
      return true;
    return (!name.equals(pair.first()));
  }

  /**
   * Checks if the name/value pair is a match with the given name. If this
   * method returns {@code true} <tt>pair.second()</tt> is usually read next.
   * 
   * @param name
   *          a name.
   * @param pair
   *          a name/value pair.
   * @return {@code true} if <tt>pair != null</tt> and <tt>name</tt> is equal to
   *         <tt>pair.first()</tt>, {@code false} otherwise.
   */
  private static boolean isFor(String name, Pair<String, String> pair) {
    if (pair == null)
      return false;
    return (name.equals(pair.first()));
  }

  /**
   * Extracts the contents of a mutable string before the next occurrence of the
   * passed separator string, not including the separator string from the passed
   * mutable string.
   * 
   * @param separator
   *          a string to use as a separator.
   * @param mutableString
   *          a mutable string.
   * @return the contents of a mutable string before the next occurrence of the
   *         passed separator string, not including the separator string from
   *         the passed mutable string.
   * 
   * @throws IllegalArgumentException
   *           if the separator is not found.
   */
  @NonNull
  static String toNext(final String separator, final StringBuilder mutableString) {
    final int sepIndex = mutableString.indexOf(separator);
    if (sepIndex == -1)
      throw new IllegalArgumentException(separator + " not found in " + mutableString);
    final String result = mutableString.substring(0, sepIndex);
    mutableString.delete(0, sepIndex + 1);
    return result;
  }
}
