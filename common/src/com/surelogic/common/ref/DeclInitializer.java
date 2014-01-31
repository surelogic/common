package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;

@Immutable
final class DeclInitializer extends Decl {

  final boolean f_isStatic;
  final boolean f_isImplicit;

  DeclInitializer(IDecl parent, List<Decl.DeclBuilder> childBuilders, boolean isStatic, boolean isImplicit) {
    super(parent, childBuilders, "");
    f_isStatic = isStatic;
    f_isImplicit = isImplicit;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.INITIALIZER;
  }

  @Override
  public boolean isStatic() {
    return f_isStatic;
  }

  @Override
  public boolean isImplicit() {
    return f_isImplicit;
  }
}
