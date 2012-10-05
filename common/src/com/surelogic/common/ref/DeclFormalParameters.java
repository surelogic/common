package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
abstract class DeclFormalParameters extends DeclVisibility {

  @NonNull
  final IDecl[] f_formalParameterTypes;

  public DeclFormalParameters(IDecl parent, String name, Visibility visibility, IDecl[] formalParameterTypes) {
    super(parent, name, visibility);
    if (formalParameterTypes == null)
      f_formalParameterTypes = EMPTY;
    else
      f_formalParameterTypes = formalParameterTypes;
  }

  @Override
  @NonNull
  public IDecl[] getFormalParameterTypes() {
    return f_formalParameterTypes;
  }
}
