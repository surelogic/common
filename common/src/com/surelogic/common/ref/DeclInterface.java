package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclInterface extends DeclVisibility {

  @NonNull
  final String f_formalTypeParameters;

  DeclInterface(IDecl parent, String name, Visibility visibility, String formalTypeParameters) {
    super(parent, name, visibility);
    f_formalTypeParameters = formalTypeParameters == null ? "" : formalTypeParameters;
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }

  @Override
  @NonNull
  public String getFormalTypeParameters() {
    return f_formalTypeParameters;
  }
}
