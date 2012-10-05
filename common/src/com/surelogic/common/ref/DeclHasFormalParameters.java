package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
abstract class DeclHasFormalParameters extends DeclVisibility {

  @NonNull
  final TypeRef[] f_formalParameterTypes;

  public DeclHasFormalParameters(IDecl parent, String name, Visibility visibility, TypeRef[] formalParameterTypes) {
    super(parent, name, visibility);
    if (formalParameterTypes == null)
      f_formalParameterTypes = TypeRef.EMPTY;
    else
      f_formalParameterTypes = formalParameterTypes;
  }

  @Override
  @NonNull
  public TypeRef[] getFormalParameterTypes() {
    return f_formalParameterTypes;
  }
}
