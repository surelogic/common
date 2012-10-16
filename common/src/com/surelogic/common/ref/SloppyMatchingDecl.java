package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

/**
 * A wrapper for an {@link IDecl} that uses sloppy matching for its
 * {@link #equals(Object)} and {@link #hashCode()}
 * implementations&mdash;allowing for a sloppy match if minor things about the
 * declaration changed..
 * <p>
 * Equality tests are done using {@link IDecl#isSameDeclarationAsSloppy(IDecl)}
 * and the hash code value is built up with
 * {@link IDecl#simpleDeclarationHashCodeSloppy()}.
 * <p>
 * <b>Warning:</b> all instances placed in a collection must be of this wrapper
 * type or the behavior of the collection is undefined. You should not mix
 * instances of this wrapper with {@link IDecl} instances obtained from
 * {@link Decl.DeclBuilder#build()}.
 */
@ValueObject
public final class SloppyMatchingDecl implements IDecl {

  public static SloppyMatchingDecl getInstance(@NonNull IDecl decl) {
    return new SloppyMatchingDecl(decl);
  }

  public SloppyMatchingDecl(@NonNull IDecl decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    f_decl = decl;
  }

  @NonNull
  private final IDecl f_decl;

  /**
   * Returns the hash code value for this declaration.
   * 
   * @return the hash code value for this declaration.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    IDecl dThis = this;
    while (dThis != null) {
      result = prime * result + dThis.simpleDeclarationHashCodeSloppy();
      dThis = dThis.getParent();
    }
    return result;
  }

  /**
   * Compares the specified object with this declaration for equality.
   * <p>
   * This method uses the exact same comparison as
   * {@link #isSameDeclarationAsSloppy(IDecl)}. In fact, the implementation of
   * this method is
   * 
   * <pre>
   * if (obj instanceof IDecl)
   *   return isSameDeclarationAsSloppy((IDecl) obj);
   * else
   *   return false;
   * </pre>
   * 
   * @param o
   *          object to be compared for equality with this declaration.
   * @return {@code true} if the specified object is sloppily equal to this
   *         declaration.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDecl)
      return isSameDeclarationAsSloppy((IDecl) obj);
    else
      return false;
  }

  @Nullable
  public IDecl getParent() {
    return f_decl.getParent();
  }

  @NonNull
  public List<IDecl> getChildren() {
    return f_decl.getChildren();
  }

  @NonNull
  public Kind getKind() {
    return f_decl.getKind();
  }

  @NonNull
  public String getName() {
    return f_decl.getName();
  }

  @Nullable
  public TypeRef getTypeOf() {
    return f_decl.getTypeOf();
  }

  @NonNull
  public Visibility getVisibility() {
    return f_decl.getVisibility();
  }

  public boolean isStatic() {
    return f_decl.isStatic();
  }

  public boolean isFinal() {
    return f_decl.isFinal();
  }

  public boolean isAbstract() {
    return f_decl.isAbstract();
  }

  public boolean isImplicit() {
    return f_decl.isImplicit();
  }

  @NonNull
  public List<IDecl> getParameters() {
    return f_decl.getParameters();
  }

  public int getPosition() {
    return f_decl.getPosition();
  }

  @NonNull
  public List<IDecl> getTypeParameters() {
    return f_decl.getTypeParameters();
  }

  @NonNull
  public List<TypeRef> getBounds() {
    return f_decl.getBounds();
  }

  public boolean hasSameAttributesAs(IDecl o) {
    return f_decl.hasSameAttributesAs(o);
  }

  public boolean hasSameAttributesAsSloppy(IDecl o) {
    return f_decl.hasSameAttributesAsSloppy(o);
  }

  public boolean isSameSimpleDeclarationAs(IDecl o) {
    return f_decl.isSameSimpleDeclarationAs(o);
  }

  public boolean isSameSimpleDeclarationAsSloppy(IDecl o) {
    return f_decl.isSameSimpleDeclarationAsSloppy(o);
  }

  public int simpleDeclarationHashCode() {
    return f_decl.simpleDeclarationHashCode();
  }

  public int simpleDeclarationHashCodeSloppy() {
    return f_decl.simpleDeclarationHashCodeSloppy();
  }

  public boolean isSameDeclarationAs(IDecl o) {
    return f_decl.isSameDeclarationAs(o);
  }

  public boolean isSameDeclarationAsSloppy(IDecl o) {
    return f_decl.isSameDeclarationAsSloppy(o);
  }

  @Override
  public String toString() {
    return f_decl.toString();
  }
}
