package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
abstract class DeclFormalParameters extends DeclVisibility {

  @NonNull
  final FormalParameter[] f_formalParameters;

  public DeclFormalParameters(IDecl parent, String name, Visibility visibility, FormalParameter[] formalParameters) {
    super(parent, name, visibility);
    if (formalParameters == null)
      f_formalParameters = FormalParameter.EMPTY;
    else
      f_formalParameters = formalParameters;
  }

  @Override
  @NonNull
  public FormalParameter[] getFormalParameters() {
    return super.getFormalParameters();
  }
}
