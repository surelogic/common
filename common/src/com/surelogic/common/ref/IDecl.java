package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.SLUtility;

/**
 * Defines an interface for declarations. This is intended to model a particular
 * declaration, through its parents. It is not intended to model <i>all</i>
 * declarations in a Java program&mdash;note the lack of an ability to get the
 * children of a declaration.
 * <p>
 * Concrete instances are constructed using the following builders:
 * <ul>
 * <li>{@link Decl.ClassBuilder}</li>
 * <li>{@link Decl.ConstructorBuilder}</li>
 * <li>{@link Decl.EnumBuilder}</li>
 * <li>{@link Decl.FieldBuilder}</li>
 * <li>{@link Decl.InitializerBuilder}</li>
 * <li>{@link Decl.InterfaceBuilder}</li>
 * <li>{@link Decl.MethodBuilder}</li>
 * <li>{@link Decl.PackageBuilder}</li>
 * <li>{@link Decl.ParameterBuilder}</li>
 * <li>{@link Decl.TypeParameterBuilder}</li>
 * </ul>
 */
@ThreadSafe
public interface IDecl {

  /**
   * Indicates where a code position is in relation to a particular declaration.
   */
  enum Position {
    ON_DECL, WITHIN_DECL
  }

  /**
   * The kind of declaration.
   */
  enum Kind {
    CLASS, CONSTRUCTOR, ENUM, FIELD, INITIALIZER, INTERFACE, METHOD, PACKAGE, PARAMETER, TYPE_PARAMETER
  }

  /**
   * The visibility of a declaration.
   */
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
   * Gets all child declarations of this declaration. The returned set is a copy
   * and may be safely mutated.
   * 
   * @return the possibly empty set of declarations that have this declaration
   *         as their parent.
   */
  @NonNull
  Set<IDecl> getChildren();

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
   * The name returned for an anonymous class is the number, starting at one,
   * indicating its order of occurrence in the class.
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
   * {@link Kind#PARAMETER}, (b) the return type for a {@link Kind#METHOD}, or
   * (c) the type bound for a {@link Kind#TYPE_PARAMETER}. In the case of a
   * {@link Kind#METHOD} a return value of {@code null} indicates the return
   * type of the method is <tt>void</tt>. In the case of a
   * {@link Kind#TYPE_PARAMETER} a return value of {@code null} indicates no
   * bound. If this is meaningless for the declaration {@code null} is returned.
   * 
   * @return the type of this declaration, {@link null} if none.
   */
  @Nullable
  TypeRef getTypeOf();

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
  Visibility getVisiblity();

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
   * Gets the ordered list, first to last, of the parameter types for a
   * {@link Kind#CONSTRUCTOR} or {@link Kind#METHOD}. If this is meaningless for
   * the declaration, an empty array is returned.
   * 
   * @return a possibly empty list of the formal parameter types, in order.
   */
  @NonNull
  TypeRef[] getParameterTypes();

  /**
   * Gets the zero-based position number of a parameter declaration&mdash;this
   * information is only meaningful for {@link Kind#PARAMETER} and
   * {@link Kind#TYPE_PARAMETER}. If this is meaningless for the declaration, -1
   * is returned.
   * 
   * @return the zero-based position number of this parameter, or -1.
   */
  int getPosition();

  /**
   * Gets the type parameters for a {@link Kind#CLASS}, {@link Kind#INTERFACE},
   * or {@link Kind#METHOD}. If this is meaningless for the declaration, an
   * empty array is returned.
   * <p>
   * The result of this method is intended for display and to help match type
   * names, such as <tt>T</tt>, used for declarations within a parameterized
   * type or method.
   * <p>
   * The table below shows several examples.
   * 
   * <table border=1>
   * <tr>
   * <th>Declaration</th>
   * <th>{@link #getName()}</th>
   * <th>{@link #getTypeParameters()}</th>
   * </tr>
   * <tr>
   * <td><tt>List&lt;E&gt;</tt></td>
   * <td><tt>List</tt></td>
   * <td><tt>&lt;E&gt;</tt></td>
   * </tr>
   * <tr>
   * <td><tt>Map&lt;K,V&gt;</tt></td>
   * <td><tt>Map</tt></td>
   * <td><tt>&lt;K,V&gt;</tt></td>
   * </tr>
   * <tr>
   * <td><tt>public &lt;T&gt; T[] toArray(T... elements) {}</tt></td>
   * <td><tt>toArray</tt></td>
   * <td><tt>&lt;T&gt;</tt></td>
   * </tr>
   * </table>
   * 
   * @return the ordered list of type parameters for this declaration.
   */
  @NonNull
  IDecl[] getTypeParameters();

  /**
   * Gets the bounds for {@link Kind#TYPE_PARAMETER}. If this is meaningless for
   * the declaration, an empty array is returned.
   * 
   * @return the bounds.
   */
  @NonNull
  TypeRef[] getBounds();
}
