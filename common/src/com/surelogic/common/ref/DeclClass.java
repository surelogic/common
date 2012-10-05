package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclClass extends DeclVisibility {

  @NonNull
  final String f_formalTypeParameters;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclClass(IDecl parent, String name, Visibility visibility, String formalTypeParameters, boolean isStatic, boolean isFinal,
      boolean isAbstract) {
    super(parent, name, visibility);
    f_formalTypeParameters = formalTypeParameters == null ? "" : formalTypeParameters;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
  }

  @NonNull
  public Kind getKind() {
    return Kind.CLASS;
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

  @Override
  String toStringHelper() {
    if (f_visibility == Visibility.ANONYMOUS)
      return ".(anonymous class)" + f_formalTypeParameters;
    else
      return "." + f_name + f_formalTypeParameters;
  }
}
