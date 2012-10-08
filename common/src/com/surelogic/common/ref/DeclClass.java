package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclClass extends DeclVisibility {

  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclClass(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, boolean isStatic,
      boolean isFinal, boolean isAbstract) {
    super(parent, childBuilders, name, visibility);
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
  }

  @NonNull
  public Kind getKind() {
    return Kind.CLASS;
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
      return ".(anonymous class)" + Decl.toStringHelperTypeParameters(this);
    else
      return "." + f_name + Decl.toStringHelperTypeParameters(this);
  }
}
