package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
abstract class DeclHasFormalParameters extends DeclVisibility {

  @NonNull
  final TypeRef[] f_formalParameterTypes;

  public DeclHasFormalParameters(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, Visibility visibility,
      TypeRef[] formalParameterTypes) {
    super(parent, childBuilders, name, visibility);
    if (formalParameterTypes == null)
      f_formalParameterTypes = TypeRef.EMPTY;
    else
      f_formalParameterTypes = formalParameterTypes;
  }

  @Override
  @NonNull
  public TypeRef[] getParameterTypes() {
    return f_formalParameterTypes;
  }
}
