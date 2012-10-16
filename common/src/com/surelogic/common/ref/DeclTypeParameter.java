package com.surelogic.common.ref;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
public final class DeclTypeParameter extends DeclWithPosition {

  @NonNull
  private final List<TypeRef> f_bounds;

  public DeclTypeParameter(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, int position, List<TypeRef> bounds) {
    super(parent, childBuilders, name, position);
    f_bounds = bounds;
  }

  @NonNull
  public Kind getKind() {
    return Kind.TYPE_PARAMETER;
  }

  @Override
  @NonNull
  public List<TypeRef> getBounds() {
    return new ArrayList<TypeRef>(f_bounds);
  }

  @Override
  String toStringHelper() {
    final StringBuilder b = new StringBuilder();
    b.append(f_name);
    if (f_bounds.size() > 0) {
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
    return b.toString();
  }
}
