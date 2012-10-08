package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclClass extends DeclVisibility {

  @NonNull
  final IDecl[] f_formalTypeParameters;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclClass(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, IDecl[] formalTypeParameters,
      boolean isStatic, boolean isFinal, boolean isAbstract) {
    super(parent, childBuilders, name, visibility);
    f_formalTypeParameters = formalTypeParameters == null ? EMPTY : formalTypeParameters;
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
  public IDecl[] getTypeParameters() {
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
