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
    final IDecl[] typeParameters = getParent().getFormalTypeParameters();
    boolean first = typeParameters[0] == this;
    boolean last = typeParameters[typeParameters.length - 1] == this;
    final StringBuilder b = new StringBuilder();
    if (first)
      b.append("<");
    else
      b.append(",");
    b.append(f_name);
    if (f_bounds != null) {
      b.append(" extends ");
      b.append(f_bounds.getFullyQualified());
    }
    if (last)
      b.append(">");
    return b.toString();
  }
}
