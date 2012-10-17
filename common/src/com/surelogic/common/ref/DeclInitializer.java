package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
class DeclInitializer extends Decl {

  final boolean f_isStatic;

  DeclInitializer(IDecl parent, List<Decl.DeclBuilder> childBuilders, boolean isStatic) {
    super(parent, childBuilders, "");
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

  @Override
  String toStringHelper() {
    return f_isStatic ? ".<static init>" : ".<init>";
  }
}
