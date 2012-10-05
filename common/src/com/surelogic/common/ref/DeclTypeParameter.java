package com.surelogic.common.ref;

import com.surelogic.NonNull;
import com.surelogic.Nullable;

public final class DeclTypeParameter extends Decl {

  @Nullable
  private final TypeRef f_bounds;

  public DeclTypeParameter(IDecl parent, String name, TypeRef bounds) {
    super(parent, name);
    f_bounds = bounds;
  }

  @NonNull
  public Kind getKind() {
    return Kind.TYPE_PARAMETER;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_bounds;
  }

  @Override
  String toStringHelper() {
    // TODO
    return "TODO";
  }
}
