package com.surelogic.common.adhoc;

import java.util.List;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.ref.DeclVisitor;
import com.surelogic.common.ref.IDecl;
import com.surelogic.common.ref.IDeclParameter;
import com.surelogic.common.ref.IDeclTypeParameter;
import com.surelogic.common.ref.TypeRef;

/**
 * This is a declaration created from Java declaration strings used by the ad
 * hoc query tool to represent Java declaration images. It is not a <b>general
 * purpose</b> declaration and many methods will throw
 * {@link UnsupportedOperationException}.
 * <p>
 * Its purpose is to be passed to the <tt>SLImages.getImageFor(IDecl)</tt>
 * method so it supports only what is needed by that method. The supported
 * methods are:
 * <ul>
 * <li>{@link IDecl#getKind()}</li>
 * <li>{@link IDecl#getVisibility()}</li>
 * <li>{@link IDecl#isAbstract()}</li>
 * <li>{@link IDecl#isFinal()}</li>
 * <li>{@link IDecl#isImplicit()}</li>
 * <li>{@link IDecl#isStatic()}</li>
 * <li>{@link IDecl#isVolatile()}</li>
 * </ul>
 */
public final class AdHocDecl implements IDecl {

  @NonNull
  final Kind f_kind;
  @NonNull
  final Visibility f_visibility;
  private final boolean f_isAbstract;
  private final boolean f_isFinal;
  private final boolean f_isImplicit;
  private final boolean f_isStatic;
  private final boolean f_isVolatile;

  /**
   * Constructs an instance using the passed encoded string. The encoded string
   * should be in the general form:
   * <p>
   * <tt>@</tt> <i>DeclarationType</i> <tt>:</tt> <i>Visibility</i> <tt>:</tt>
   * <i>Modifiers</i>
   * <p>
   * See the Wiki for the ad hoc query tool and the below implementation for
   * specifics of the encoding.
   * <p>
   * If the encoded string is {@code null} or is not understandable then the
   * resulting object represents a public class with no modifiers.
   * 
   * @param encoded
   *          an encoded string describing a Java declaration.
   */
  public AdHocDecl(@Nullable String encoded) {
    if (encoded == null || !encoded.startsWith("@"))
      encoded = "@CL";
    String b = encoded.substring(1);
    if (b.length() < 2) {
      f_kind = Kind.CLASS;
      f_visibility = Visibility.PUBLIC;
      f_isAbstract = false;
      f_isFinal = false;
      f_isImplicit = false;
      f_isStatic = false;
      f_isVolatile = false;
    } else {
      // Declaration type
      final String dt = b.substring(0, 2);
      if ("AN".equalsIgnoreCase(dt))
        f_kind = Kind.ANNOTATION;
      else if ("CL".equalsIgnoreCase(dt))
        f_kind = Kind.CLASS;
      else if ("CO".equalsIgnoreCase(dt))
        f_kind = Kind.CONSTRUCTOR;
      else if ("EN".equalsIgnoreCase(dt))
        f_kind = Kind.ENUM;
      else if ("FL".equalsIgnoreCase(dt))
        f_kind = Kind.FIELD;
      else if ("IT".equalsIgnoreCase(dt))
        f_kind = Kind.INITIALIZER;
      else if ("IN".equalsIgnoreCase(dt))
        f_kind = Kind.INTERFACE;
      else if ("ME".equalsIgnoreCase(dt))
        f_kind = Kind.METHOD;
      else if ("PK".equalsIgnoreCase(dt))
        f_kind = Kind.PACKAGE;
      else if ("PA".equalsIgnoreCase(dt))
        f_kind = Kind.PARAMETER;
      else if ("TP".equalsIgnoreCase(dt))
        f_kind = Kind.TYPE_PARAMETER;
      else
        f_kind = Kind.CLASS;
      b = b.substring(2);
      if (!b.startsWith(":") || b.length() < 3) {
        f_visibility = Visibility.PUBLIC;
        f_isAbstract = false;
        f_isFinal = false;
        f_isImplicit = false;
        f_isStatic = false;
        f_isVolatile = false;
      } else {
        // Visibility
        final String vs = b.substring(1, 3);
        if ("AN".equalsIgnoreCase(vs))
          f_visibility = Visibility.ANONYMOUS;
        else if ("DE".equalsIgnoreCase(vs))
          f_visibility = Visibility.DEFAULT;
        else if ("PR".equalsIgnoreCase(vs))
          f_visibility = Visibility.PRIVATE;
        else if ("PO".equalsIgnoreCase(vs))
          f_visibility = Visibility.PROTECTED;
        else if ("PU".equalsIgnoreCase(vs))
          f_visibility = Visibility.PUBLIC;
        else if ("NA".equalsIgnoreCase(vs))
          f_visibility = Visibility.NA;
        else
          f_visibility = Visibility.PUBLIC;
        b = b.substring(3, b.length());
        if (!b.startsWith(":") || b.length() < 2) {
          f_isAbstract = false;
          f_isFinal = false;
          f_isImplicit = false;
          f_isStatic = false;
          f_isVolatile = false;
        } else {
          // Modifiers
          final String mods = b.substring(1);
          f_isAbstract = mods.contains("A") || mods.contains("a");
          f_isFinal = mods.contains("F") || mods.contains("f");
          f_isImplicit = mods.contains("I") || mods.contains("i");
          f_isStatic = mods.contains("S") || mods.contains("s");
          f_isVolatile = mods.contains("V") || mods.contains("v");
        }
      }
    }
  }

  @Nullable
  public IDecl getParent() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public List<IDecl> getChildren() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public Kind getKind() {
    return f_kind;
  }

  @NonNull
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  public TypeRef getTypeOf() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public Visibility getVisibility() {
    return f_visibility;
  }

  public boolean isStatic() {
    return f_isStatic;
  }

  public boolean isFinal() {
    return f_isFinal;
  }

  public boolean isAbstract() {
    return f_isAbstract;
  }

  public boolean isImplicit() {
    return f_isImplicit;
  }

  public boolean isVolatile() {
    return f_isVolatile;
  }

  @NonNull
  public List<IDeclParameter> getParameters() {
    throw new UnsupportedOperationException();
  }

  public int getPosition() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public List<IDeclTypeParameter> getTypeParameters() {
    throw new UnsupportedOperationException();
  }

  @NonNull
  public List<TypeRef> getBounds() {
    throw new UnsupportedOperationException();
  }

  public boolean hasSameAttributesAs(IDecl o) {
    throw new UnsupportedOperationException();
  }

  public boolean hasSameAttributesAsSloppy(IDecl o) {
    throw new UnsupportedOperationException();
  }

  public boolean isSameSimpleDeclarationAs(IDecl o) {
    throw new UnsupportedOperationException();
  }

  public boolean isSameSimpleDeclarationAsSloppy(IDecl o, boolean checkAttributes) {
    throw new UnsupportedOperationException();
  }

  public int simpleDeclarationHashCode() {
    throw new UnsupportedOperationException();
  }

  public int simpleDeclarationHashCodeSloppy() {
    throw new UnsupportedOperationException();
  }

  public boolean isSameDeclarationAs(IDecl o) {
    throw new UnsupportedOperationException();
  }

  public boolean isSameDeclarationAsSloppy(IDecl o) {
    throw new UnsupportedOperationException();
  }

  public void acceptThisToRoot(@NonNull DeclVisitor visitor) {
    throw new UnsupportedOperationException();

  }

  public void acceptRootToThis(@NonNull DeclVisitor visitor) {
    throw new UnsupportedOperationException();
  }
}
