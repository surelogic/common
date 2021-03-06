package com.surelogic.common.ref;

import com.surelogic.NonNull;
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
 */
@ValueObject
public final class SloppyWrapper<T extends IDecl> {

  public static <T extends IDecl> SloppyWrapper<T> getInstance(@NonNull T decl) {
    return new SloppyWrapper<>(decl);
  }

  public SloppyWrapper(@NonNull T decl) {
    if (decl == null)
      throw new IllegalArgumentException(I18N.err(44, "decl"));
    f_decl = decl;
  }

  @NonNull
  private final T f_decl;

  /**
   * Gets the wrapped Java declaration.
   * 
   * @return an {@link IDecl} instance.
   */
  @NonNull
  public T getDecl() {
    return f_decl;
  }

  /**
   * Returns the hash code value for this declaration.
   * 
   * @return the hash code value for this declaration.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    IDecl dThis = f_decl;
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
   * {@link #isSameDeclarationAsSloppy(IDecl)}.
   * 
   * @param o
   *          object to be compared for equality with this declaration. Should
   *          be either an {@link SloppyWrapper} instance or an {@link IDecl}
   *          instance&mdash;both are handled correctly.
   * @return {@code true} if the specified object is sloppily equal to this
   *         declaration.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SloppyWrapper) {
      return f_decl.isSameDeclarationAsSloppy(((SloppyWrapper<?>) obj).getDecl());
    } else if (obj instanceof IDecl) {
      return f_decl.isSameDeclarationAsSloppy((IDecl) obj);
    } else
      return false;
  }
}
