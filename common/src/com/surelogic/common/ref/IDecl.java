package com.surelogic.common.ref;

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
 * <li>{@link Decl.PackageBuilder}</li>
 * </ul>
 */
@ThreadSafe
public interface IDecl {

  enum Kind {
    CLASS, CONSTRUCTOR, ENUM, FIELD, INITIALIZER, INTERFACE, METHOD, PACKAGE, PARAMETER
  }

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
   * Gets the type of this for a {@link Kind#FIELD} or {@link Kind#PARAMETER}.
   * If this is meaningless for the declaration {@code null} is returned.
   * 
   * @return the type of this declaration, {@link null} if none. All elements
   *         returned will be of {@link Kind#CLASS}, {@link Kind#INTERFACE}, or
   *         {@link Kind#ENUM}.
   */
  @Nullable
  IDecl getTypeOf();

  /**
   * Gets the formal type parameters for a {@link Kind#CLASS},
   * {@link Kind#INTERFACE}, or {@link Kind#METHOD}. If this is meaningless for
   * the declaration, an empty string is returned.
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
   * <th>{@link #getFormalTypeParameters()}</th>
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
   * @return the formal type parameters for this declaration.
   */
  @NonNull
  String getFormalTypeParameters();

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
   * Gets the list of formal parameters for a {@link Kind#CONSTRUCTOR} or
   * {@link Kind#METHOD}. If this is meaningless for the declaration, an empty
   * array is returned.
   * 
   * @return a possibly empty list of formal parameters. All elements returned
   *         will be of {@link Kind#PARAMETER} and {@link #getParent()}
   *         <tt>== this</tt>.
   */
  IDecl[] getFormalParameters();
}
