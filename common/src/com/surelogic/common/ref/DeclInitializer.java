package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclInitializer extends Decl {

  final boolean f_isStatic;

  DeclInitializer(IDecl parent, String name, boolean isStatic) {
    super(parent, name);
    f_isStatic = isStatic;
  }

  @NonNull
  public Kind getKind() {
    return Kind.INITIALIZER;
  }

  @Override
  public boolean isStatic() {
    return f_isStatic;
  }
}
