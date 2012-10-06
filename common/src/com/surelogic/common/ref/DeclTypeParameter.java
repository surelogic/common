package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.NonNull;
import com.surelogic.Nullable;

public final class DeclTypeParameter extends Decl {

  @Nullable
  private final TypeRef[] f_bounds;

  public DeclTypeParameter(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, TypeRef[] bounds) {
    super(parent, childBuilders, name);
    f_bounds = bounds;
  }

  @NonNull
  public Kind getKind() {
    return Kind.TYPE_PARAMETER;
  }

  @Override
  @NonNull
  public TypeRef[] getBounds() {
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
    if (f_bounds.length > 0) {
      b.append(" extends ");
      boolean firstBound = true;
      for (TypeRef bound : f_bounds) {
        if (firstBound)
          firstBound = false;
        else
          b.append(" & ");
        b.append(bound.getFullyQualified());
      }
    }
    if (last)
      b.append(">");
    return b.toString();
  }
}
