package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;

@Immutable
final class DeclParameter extends Decl {

  @NonNull
  final IDecl f_typeOf;
  final boolean f_isFinal;

  DeclParameter(IDecl parent, String name, IDecl typeOf, boolean isFinal) {
    super(parent, name);
    if (typeOf == null)
      throw new IllegalArgumentException(I18N.err(44, "typeOf"));
    f_typeOf = typeOf;
    f_isFinal = isFinal;
  }

  @NonNull
  public Kind getKind() {
    return Kind.PARAMETER;
  }

  @Override
  @Nullable
  public IDecl getTypeOf() {
    return f_typeOf;
  }

  @Override
  public boolean isFinal() {
    return f_isFinal;
  }
}
