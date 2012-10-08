package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclInterface extends DeclVisibility {

  @NonNull
  final IDecl[] f_formalTypeParameters;

  DeclInterface(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, IDecl[] formalTypeParameters) {
    super(parent, childBuilders, name, visibility);
    f_formalTypeParameters = formalTypeParameters == null ? EMPTY : formalTypeParameters;
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }

  @Override
  @NonNull
  public IDecl[] getTypeParameters() {
    return f_formalTypeParameters;
  }

  @Override
  String toStringHelper() {
    return "." + f_name + f_formalTypeParameters;
  }
}
