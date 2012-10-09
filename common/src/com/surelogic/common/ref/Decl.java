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
import com.surelogic.common.SLUtility;
import com.surelogic.common.i18n.I18N;

@Immutable
public abstract class Decl implements IDecl {

  /**
   * Constructs class {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class ClassBuilder extends DeclBuilder {

    Visibility f_visibility;
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
          throw new IllegalArgumentException(I18N.err(265, f_name));
      }

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      // a top-level class can never be static
      if (parent.getKind() == Kind.PACKAGE)
        f_isStatic = false;

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(266, f_name));

      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(274, position));
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

    Visibility f_visibility;

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
        throw new IllegalArgumentException(I18N.err(262, f_name));

      final Set<Integer> usedParmPositions = new HashSet<Integer>();
      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof ParameterBuilder) {
          final Integer position = Integer.valueOf(((ParameterBuilder) b).f_position);
          if (usedParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(273, position));
          usedParmPositions.add(position);
        } else if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(274, position));
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

    Visibility f_visibility;

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
    public IDecl buildInternal(IDecl parent) {
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

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

    Visibility f_visibility;
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
    public IDecl buildInternal(IDecl parent) {
      if (f_typeOf == null)
        throw new IllegalArgumentException(I18N.err(44, "typeOf"));
      if (!SLUtility.isValidJavaIdentifier(f_name))
        throw new IllegalArgumentException(I18N.err(265, f_name));
      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

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
        throw new IllegalArgumentException(I18N.err(262, f_name));

      return new DeclInitializer(parent, f_childBuilders, f_isStatic);
    }
  }

  /**
   * Constructs interface {@link IDecl} instances.
   */
  @NotThreadSafe
  public static final class InterfaceBuilder extends DeclBuilder {

    Visibility f_visibility;

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
        throw new IllegalArgumentException(I18N.err(265, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

      // NA is not allowed
      if (f_visibility == Visibility.NA)
        f_visibility = Visibility.PUBLIC;

      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(274, position));
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

    Visibility f_visibility;
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
        throw new IllegalArgumentException(I18N.err(265, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

      if (f_isAbstract && f_isFinal)
        throw new IllegalArgumentException(I18N.err(269, f_name));

      if (f_isAbstract && f_isStatic)
        throw new IllegalArgumentException(I18N.err(270, f_name));

      final Set<Integer> usedParmPositions = new HashSet<Integer>();
      final Set<Integer> usedTypeParmPositions = new HashSet<Integer>();
      for (DeclBuilder b : f_childBuilders) {
        if (b instanceof ParameterBuilder) {
          final Integer position = Integer.valueOf(((ParameterBuilder) b).f_position);
          if (usedParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(273, position));
          usedParmPositions.add(position);
        } else if (b instanceof TypeParameterBuilder) {
          final Integer position = Integer.valueOf(((TypeParameterBuilder) b).f_position);
          if (usedTypeParmPositions.contains(position))
            throw new IllegalArgumentException(I18N.err(274, position));
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
        throw new IllegalArgumentException(I18N.err(264, f_name, parent));

      if (f_name == null || "".equals(f_name)) {
        f_name = SLUtility.JAVA_DEFAULT_PACKAGE;
      } else {
        // check the name contains valid identifiers between dots
        final StringBuilder b = new StringBuilder(f_name);
        boolean done = false;
        while (!done) {
          final int dotIndex = b.lastIndexOf(".");
          final String name;
          if (dotIndex == -1) {
            name = b.toString();
            done = true;
          } else {
            name = b.substring(dotIndex + 1);
            b.delete(dotIndex, b.length());
          }
          if (!SLUtility.isValidJavaIdentifier(name))
            throw new IllegalArgumentException(I18N.err(265, name, f_name));
        }
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
    boolean f_isFinal;

    /**
     * Constructs a parameter builder.
     * 
     * @param position
     *          the zero-based argument number of this parameter.
     */
    public ParameterBuilder(int position) {
      f_position = position;
    }

    /**
     * Constructs a parameter builder.
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
          throw new IllegalArgumentException(I18N.err(265, f_name));
      }

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

      final Kind parentKind = parent.getKind();
      if (!(parentKind == Kind.CONSTRUCTOR || parentKind == Kind.METHOD))
        throw new IllegalArgumentException(I18N.err(267, f_name, parentKind));

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
    boolean f_isFinal;

    /**
     * Constructs a type parameter builder.
     * 
     * @param position
     *          the zero-based argument number of this parameter.
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
        throw new IllegalArgumentException(I18N.err(265, f_name));

      if (parent == null)
        throw new IllegalArgumentException(I18N.err(262, f_name));

      final Kind parentKind = parent.getKind();
      if (!(parentKind == Kind.CLASS || parentKind == Kind.INTERFACE || parentKind == Kind.METHOD || parentKind == Kind.CONSTRUCTOR))
        throw new IllegalArgumentException(I18N.err(263, f_name, parentKind));

      return new DeclTypeParameter(parent, f_childBuilders, f_name, f_position, f_bounds);
    }
  }

  public static abstract class DeclBuilder {

    DeclBuilder f_parent;
    final Set<DeclBuilder> f_childBuilders = new HashSet<DeclBuilder>();
    String f_name;

    /**
     * Only valid after the build.
     */
    IDecl f_declaration;

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
      if (value != null)
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
  public Decl(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name) {
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
  public final Set<IDecl> getChildren() {
    if (f_children == null)
      return Collections.emptySet();
    else {
      final Set<IDecl> result = new HashSet<IDecl>();
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
}
