package com.surelogic.common.ref;

import java.util.EnumSet;
import java.util.List;

import com.surelogic.*;
import com.surelogic.common.SLUtility;

/**
 * Defines an interface for declarations. This is intended to model a particular
 * declaration, through its parents. It is not intended to model <i>all</i>
 * declarations in a Java program&mdash;note the lack of an ability to get the
 * children of a declaration.
 * <p>
 * Many helpful methods to pull information from a declaration are provided in
 * {@link DeclUtil}.
 * <p>
 * It is also possible to extend {@link DeclVisitor} and use either
 * {@link #acceptRootToThis(DeclVisitor)} or
 * {@link #acceptThisToRoot(DeclVisitor)} to perform useful traversals of a
 * complex declaration.
 * <p>
 * Concrete instances are constructed using the following builders:
 * <ul>
 * <li>{@link Decl.AnnotationBuilder}</li>
 * <li>{@link Decl.ClassBuilder}</li>
 * <li>{@link Decl.ConstructorBuilder}</li>
 * <li>{@link Decl.EnumBuilder}</li>
 * <li>{@link Decl.FieldBuilder}</li>
 * <li>{@link Decl.InitializerBuilder}</li>
 * <li>{@link Decl.InterfaceBuilder}</li>
 * <li>{@link Decl.LambdaBuilder}</li>
 * <li>{@link Decl.MethodBuilder}</li>
 * <li>{@link Decl.PackageBuilder}</li>
 * <li>{@link Decl.ParameterBuilder}</li>
 * <li>{@link Decl.TypeParameterBuilder}</li>
 * </ul>
 * 
 * @see DeclUtil
 */
@Immutable(appliesTo = Part.Instance)
public interface IDecl {
  public static final String ARRAY_BRACKETS = "[]";

  /**
   * The kind of declaration.
   */
  @Immutable
  enum Kind {
    ANNOTATION, CLASS, CONSTRUCTOR, ENUM, FIELD, INITIALIZER, INTERFACE, LAMBDA, METHOD, PACKAGE, PARAMETER, TYPE_PARAMETER
  }

  /*
   * Some helpful enum sets
   */

  public static final EnumSet<IDecl.Kind> IS_TYPE = EnumSet.of(IDecl.Kind.ANNOTATION, IDecl.Kind.CLASS, IDecl.Kind.ENUM,
      IDecl.Kind.INTERFACE);
  public static final EnumSet<IDecl.Kind> IS_PKG_OR_TYPE = EnumSet.of(IDecl.Kind.ANNOTATION, IDecl.Kind.CLASS, IDecl.Kind.ENUM,
      IDecl.Kind.INTERFACE, IDecl.Kind.PACKAGE);
  public static final EnumSet<IDecl.Kind> HAS_CONTROL_FLOW = EnumSet.of(IDecl.Kind.CONSTRUCTOR, IDecl.Kind.INITIALIZER,
      IDecl.Kind.LAMBDA, IDecl.Kind.METHOD);
  public static final EnumSet<IDecl.Kind> HAS_PARAMETERS = EnumSet.of(IDecl.Kind.CONSTRUCTOR, IDecl.Kind.LAMBDA, IDecl.Kind.METHOD);
  public static final EnumSet<IDecl.Kind> HAS_TYPE_PARAMETERS = EnumSet.of(IDecl.Kind.CLASS, IDecl.Kind.INTERFACE,
      IDecl.Kind.CONSTRUCTOR, IDecl.Kind.METHOD);
  public static final EnumSet<IDecl.Kind> IS_PARAMETER = EnumSet.of(IDecl.Kind.PARAMETER, IDecl.Kind.TYPE_PARAMETER);

  /**
   * The visibility of a declaration.
   */
  @Immutable
  enum Visibility {
    ANONYMOUS, DEFAULT, PRIVATE, PROTECTED, PUBLIC, NA
  }

  /**
   * Gets the declaration that this declaration is within.
   * 
   * @return the declaration that this declaration is within, or {@code null} if
   *         at the root.
   */
  @Nullable
  IDecl getParent();

  /**
   * Gets all child declarations of this declaration. The returned list is a
   * copy and may be safely mutated.
   * 
   * @return the possibly empty list of declarations that have this declaration
   *         as their parent. The returned list will have no duplicate elements.
   */
  @NonNull
  List<IDecl> getChildren();

  /**
   * Gets the kind of this declaration.
   * 
   * @return the kind of this declaration.
   */
  @NonNull
  Kind getKind();

  /**
   * Gets the lexical name of this declaration. The name does not include any
   * formal type parameters (see {@link #getFormalParameters()}).
   * <p>
   * The name returned for the default package is
   * {@link SLUtility#JAVA_DEFAULT_PACKAGE}.
   * <p>
   * The name returned for an anonymous class is the simple name of the class
   * followed by a dollar sign then the the zero-based position of the anonymous
   * class declaration within the immediate enclosing declaration. For example,
   * <tt>Runnable$4</tt>, or <tt>ActionListener$0</tt>. This convention is
   * slightly different than standard Java/Eclipse naming where the number is a
   * position within the compilation unit not the immediately enclosing
   * declaration.
   * <p>
   * The name returned for a lambda is <tt>lambda</tt> followed by a dollar sign
   * then the the zero-based position of the lambda declaration within the
   * immediate enclosing declaration. For example, <tt>lambda$0</tt>, or
   * <tt>lambda$2</tt>. This convention is slightly different than standard
   * Java/Eclipse naming where the number is a position within the compilation
   * unit not the immediately enclosing declaration.
   * <p>
   * The name returned for a constructor is the simple name of its enclosing
   * type.
   * <p>
   * The name returned for a parameter is its formal name, if known, or
   * <tt>arg</tt><i>n</i>, where <i>n</i> is the number, starting at zero,
   * indicating its order of occurrence in the list of parameters.
   * <p>
   * The name returned for any {@link Kind#INITIALIZER} is the empty string.
   * 
   * @return the lexical name of this declaration.
   */
  @NonNull
  String getName();

  /**
   * Gets (a) the type of this for a {@link Kind#FIELD} or
   * {@link Kind#PARAMETER}, or (b) the return type for a {@link Kind#METHOD} or
   * {@link Kind#LAMBDA}. In the case of a {@link Kind#METHOD} or
   * {@link Kind#LAMBDA} a result of {@code null} indicates the return type of
   * the method is <tt>void</tt>. For an anonymous class this returns the type
   * of the anonymous class expression. If this is meaningless for the
   * declaration {@code null} is returned.
   * <p>
   * Note that for a lambda this method returns the return type of the single
   * method within the functional interface that the lambda is an instance of.
   * To determine what the type of the functional interface is use
   * {@link #getLambdaFunctionalInterfaceTypeOf()}.
   * 
   * @return the type of this declaration, {@link null} if none.
   */
  @Nullable
  TypeRef getTypeOf();

  /**
   * Gets the functional interface type of this lambda. A lambda is an instance
   * of a functional interface, such as {@link Runnable}. If this is meaningless
   * for the declaration, {@code null} is returned.
   * 
   * @return the functional interface type of this lambda, or {@code null} if
   *         the declaration is not a lambda.
   */
  @Nullable
  TypeRef getLambdaFunctionalInterfaceTypeOf();

  /**
   * Gets the visibility of this declaration. If this is meaningless for the
   * declaration, {@link Visibility#NA} is returned (e.g., {@link Kind#PACKAGE}
   * and {@link Kind#PARAMETER}).
   * <p>
   * As a special case we return {@link Visibility#ANONYMOUS} for an anonymous
   * class.
   * 
   * @return the visibility of this declaration.
   */
  @NonNull
  Visibility getVisibility();

  /**
   * Gets if this declaration is declared to be <i>static</i>. If this is
   * meaningless for the declaration, {@code false} is returned.
   * 
   * @return {@code true} if this declaration is declared to be <i>static</i>,
   *         {@code false} otherwise.
   */
  boolean isStatic();

  /**
   * Gets if this declaration is declared to be <i>final</i>. If this is
   * meaningless for the declaration, {@code false} is returned.
   * 
   * @return {@code true} if this declaration is declared to be <i>final</i>,
   *         {@code false} otherwise.
   */
  boolean isFinal();

  /**
   * Gets if this declaration is declared to be <i>abstract</i>. If this is
   * meaningless for the declaration, {@code false} is returned.
   * 
   * @return {@code true} if this declaration is declared to be <i>abstract</i>,
   *         {@code false} otherwise.
   */
  boolean isAbstract();

  /**
   * Gets if this constructor declaration does <i>not</i> appear in source code,
   * it is an implicit declaration. If this is meaningless for the declaration,
   * {@code false} is returned.
   * 
   * @return {@code true} if this declaration does <i>not</i> appear in source
   *         code, {@code false} otherwise.
   */
  boolean isImplicit();

  /**
   * Gets if this declaration is declared to be <i>volatile</i>. This only
   * applies to field declarations. If this is meaningless for the declaration,
   * {@code false} is returned.
   * 
   * @return {@code true} if this declaration is declared to be <i>volatile</i>,
   *         {@code false} otherwise.
   */
  boolean isVolatile();

  /**
   * Gets if this declaration is declared to be <i>default</i>. This only
   * applies to method declarations within an interface in Java 8. If this is
   * meaningless for the declaration, {@code false} is returned.
   * 
   * @return {@code true} if this declaration is declared to be <i>default</i>,
   *         {@code false} otherwise.
   */
  boolean isDefault();

  /**
   * Gets the ordered list, first to last, of the parameter declarations for a
   * {@link Kind#CONSTRUCTOR}, {@link Kind#LAMBDA}, or {@link Kind#METHOD}. If
   * this is meaningless for the declaration, an empty array is returned. The
   * returned set is a copy and may be safely mutated.
   * 
   * @return a possibly empty list of the formal parameter types, in order.
   * 
   * @see #HAS_PARAMETERS
   */
  @NonNull
  List<IDeclParameter> getParameters();

  /**
   * For parameter lists, indicated by {@link Kind#PARAMETER} or
   * {@link Kind#TYPE_PARAMETER}, this method gets the zero-based position
   * number of the declaration.
   * <p>
   * For anonymous classes, indicated by {@link Kind#CLASS} with
   * {@link Visibility#ANONYMOUS}, this value indicates the zero-based position
   * of an anonymous class declaration within the immediate enclosing
   * declaration.
   * <p>
   * For lambda expressions, indicated by {@link Kind#LAMBDA}, this this value
   * indicates the zero-based position of the lambda expression within the
   * immediate enclosing declaration.
   * <p>
   * If this is meaningless for the declaration, -1 is returned.
   * 
   * @return the zero-based position number of this parameter or the zero-based
   *         position of this anonymous class or lambda expression within the
   *         immediate enclosing declaration, or -1.
   */
  int getPosition();

  /**
   * Gets the type parameters for a {@link Kind#CLASS}, {@link Kind#INTERFACE},
   * {@link Kind#METHOD}, or {@link Kind#CONSTRUCTOR}. If this is meaningless
   * for the declaration, an empty array is returned. The returned set is a copy
   * and may be safely mutated.
   * 
   * @return the ordered list of type parameters for this declaration.
   * 
   * @see #HAS_TYPE_PARAMETERS
   */
  @NonNull
  List<IDeclTypeParameter> getTypeParameters();

  /**
   * Gets the bounds for {@link Kind#TYPE_PARAMETER}. If this is meaningless for
   * the declaration, an empty array is returned. The returned set is a copy and
   * may be safely mutated.
   * 
   * @return the bounds.
   */
  @NonNull
  List<TypeRef> getBounds();

  /*
   * Various useful comparison methods
   */

  /**
   * Checks if this declaration's attributes are the same as those of the passed
   * declaration.
   * <p>
   * In particular, the following values are compared:
   * <ul>
   * <li>{@link #getKind()}</li>
   * <li>{@link #getName()}</li>
   * <li>{@link #getTypeOf()}</li>
   * <li>{@link #getVisibility()}</li>
   * <li>{@link #isStatic()}</li>
   * <li>{@link #isFinal()}</li>
   * <li>{@link #isAbstract()}</li>
   * <li>{@link #isImplicit()}</li>
   * <li>{@link #isDefault()}</li>
   * <li>{@link #getPosition()}</li>
   * <li>{@link #getBounds()}</li>
   * </ul>
   * The parent and children of this declaration are <b>not</b> examined by this
   * method.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this declaration's attributes are the same as those
   *         of the passed declaration, {@code false} otherwise.
   */
  boolean hasSameAttributesAs(IDecl o);

  /**
   * Checks if this declaration's attributes are the same as those of the passed
   * declaration&mdash;allowing for a sloppy match if minor things about the
   * declaration changed.
   * <p>
   * In particular, the following values are compared:
   * <ul>
   * <li>{@link #getKind()}</li>
   * <li>{@link #getName()}</li>
   * </ul>
   * The parent and children of this declaration are <b>not</b> examined by this
   * method.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this declaration's attributes are sloppily the same
   *         as those of the passed declaration, {@code false} otherwise.
   */
  public boolean hasSameAttributesAsSloppy(IDecl o);

  /**
   * Checks if this simple declaration is the same as the passed declaration. By
   * <i>simple</i> we mean that the enclosing, or parent, declarations are not
   * considered.
   * <p>
   * In particular, the two declarations are compared with
   * {@link #hasSameAttributesAs(IDecl)}. If that comparison passes then the
   * parameters, if any, and type parameters, if any, are compared. If these
   * match as well the two simple declarations are considered the same.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this simple declaration is the same as the passed
   *         declaration, {@code false} otherwise.
   */
  boolean isSameSimpleDeclarationAs(IDecl o);

  /**
   * Checks if this simple declaration is the same as the passed
   * declaration&mdash;allowing for a sloppy match if minor things about the
   * declaration changed. By <i>simple</i> we mean that the enclosing, or
   * parent, declarations are not considered.
   * <p>
   * In particular, the two declarations are compared with
   * {@link #hasSameAttributesAsSloppy(IDecl)} if <tt>checkAttributes</tt> is
   * {@code true}. If that comparison passes, or is skipped because
   * <tt>checkAttributes</tt> was {@code false}, then the parameters, if any,
   * are compared (by only their simple, or compact, type). If these match as
   * well the two simple declarations are considered the same.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this simple declaration is sloppily the same as the
   *         passed declaration, {@code false} otherwise.
   */
  public boolean isSameSimpleDeclarationAsSloppy(IDecl o, boolean checkAttributes);

  /**
   * Returns a hash code value for this declaration that is consistent with the
   * equality result of {@link #isSameSimpleDeclarationAs(IDecl)}.
   * 
   * @return a hash code value for this declaration that is consistent with the
   *         equality result of {@link #isSameSimpleDeclarationAs(IDecl)}.
   */
  int simpleDeclarationHashCode();

  /**
   * Returns a hash code value for this declaration that is consistent with the
   * equality result of {@link #isSameSimpleDeclarationAsSloppy(IDecl)}.
   * 
   * @return a hash code value for this declaration that is consistent with the
   *         equality result of {@link #isSameSimpleDeclarationAsSloppy(IDecl)}.
   */
  int simpleDeclarationHashCodeSloppy();

  /**
   * Checks if this declaration is the same as the passed declaration. The
   * enclosing, or parent, declarations are considered&mdash;this method
   * compares fully-qualified declarations.
   * <p>
   * In particular, the two declarations are compared with
   * {@link #isSameSimpleDeclarationAs(IDecl)}. If that comparison passes then
   * the same check is made on the parent of this declaration against the parent
   * of the passed declaration, and so on.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this declaration is the same as the passed
   *         declaration, {@code false} otherwise.
   */
  boolean isSameDeclarationAs(IDecl o);

  /**
   * Checks if this declaration is the same as the passed
   * declaration&mdash;allowing for a sloppy match if minor things about the
   * declaration changed. The enclosing, or parent, declarations are
   * considered&mdash;this method compares fully-qualified declarations.
   * <p>
   * In particular, the two declarations are compared with
   * {@link #isSameSimpleDeclarationAsSloppy(IDecl)}. If that comparison passes
   * then the same check is made on the parent of this declaration against the
   * parent of the passed declaration, and so on.
   * 
   * @param o
   *          any declaration.
   * @return {@code true} if this declaration is sloppily the same as the passed
   *         declaration, {@code false} otherwise.
   */
  boolean isSameDeclarationAsSloppy(IDecl o);

  /**
   * Compares the specified object with this declaration for equality.
   * <p>
   * Implementations returned from {@link Decl.DeclBuilder#build()} uses the
   * exact same comparison as {@link #isSameDeclarationAs(IDecl)}. In fact, the
   * implementation of this method is
   * 
   * <pre>
   * if (obj instanceof IDecl)
   *   return isSameDeclarationAs((IDecl) obj);
   * else
   *   return false;
   * </pre>
   * 
   * @param o
   *          object to be compared for equality with this declaration.
   * @return {@code true} if the specified object is equal to this declaration.
   */
  @Override
  boolean equals(Object o);

  /**
   * Returns the hash code value for this declaration.
   * 
   * @return the hash code value for this declaration.
   */
  @Override
  int hashCode();

  /**
   * Accepts on this visitor from this declaration, through its parents, to the
   * root declaration.
   * <p>
   * Despite the fact that this proceeds from inner to outer, parameters and
   * type parameters are visited in their position order.
   * 
   * @param visitor
   *          a visitor implementation.
   */
  void acceptThisToRoot(@NonNull DeclVisitor visitor);

  /**
   * Accepts on this visitor from the root declaration, through this
   * declaration's ancestors, to this declaration.
   * 
   * @param visitor
   *          a visitor implementation.
   */
  void acceptRootToThis(@NonNull DeclVisitor visitor);
}
