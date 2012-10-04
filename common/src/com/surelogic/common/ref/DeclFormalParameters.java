package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
abstract class DeclFormalParameters extends DeclVisibility {

  @NonNull
  final IDecl[] f_formalParameters;

  public DeclFormalParameters(IDecl parent, String name, Visibility visibility, IDecl[] formalParameters) {
    super(parent, name, visibility);
    if (formalParameters == null)
      f_formalParameters = EMPTY;
    else
      f_formalParameters = formalParameters;
  }

  @Override
  @NonNull
  public IDecl[] getFormalParameters() {
    return super.getFormalParameters();
  }
}
