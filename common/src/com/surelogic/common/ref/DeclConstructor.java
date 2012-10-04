package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclConstructor extends DeclFormalParameters {

  DeclConstructor(IDecl parent, Visibility visibility, FormalParameter[] formalParameters) {
    super(parent, parent.getName(), visibility, formalParameters);
  }

  @NonNull
  public Kind getKind() {
    return Kind.CONSTRUCTOR;
  }
}
