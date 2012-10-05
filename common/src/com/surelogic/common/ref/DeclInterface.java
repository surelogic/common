package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclInterface extends DeclVisibility {

  @NonNull
  final IDecl[] f_formalTypeParameters;

  DeclInterface(IDecl parent, String name, Visibility visibility, IDecl[] formalTypeParameters) {
    super(parent, name, visibility);
    f_formalTypeParameters = formalTypeParameters == null ? EMPTY : formalTypeParameters;
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }

  @Override
  @NonNull
  public IDecl[] getFormalTypeParameters() {
    return f_formalTypeParameters;
  }

  @Override
  String toStringHelper() {
    return "." + f_name + f_formalTypeParameters;
  }
}
