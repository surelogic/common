package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclMethod extends DeclFormalParameters {

  @NonNull
  final String f_formalTypeParameters;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclMethod(IDecl parent, String name, Visibility visibility, IDecl[] formalParameterTypes, String formalTypeParameters,
      boolean isStatic, boolean isFinal, boolean isAbstract) {
    super(parent, name, visibility, formalParameterTypes);
    f_formalTypeParameters = formalTypeParameters == null ? "" : formalTypeParameters;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
  }

  @NonNull
  public Kind getKind() {
    return Kind.METHOD;
  }

  @Override
  @NonNull
  public String getFormalTypeParameters() {
    return f_formalTypeParameters;
  }

  @Override
  public boolean isStatic() {
    return f_isStatic;
  }

  @Override
  public boolean isFinal() {
    return f_isFinal;
  }

  @Override
  public boolean isAbstract() {
    return f_isAbstract;
  }
}
