package com.surelogic.common.ref;

import java.util.ArrayList;
import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclTypeParameter extends DeclWithPosition implements IDeclTypeParameter {

  @NonNull
  private final List<TypeRef> f_bounds;

  DeclTypeParameter(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, int position, List<TypeRef> bounds) {
    super(parent, childBuilders, name, position);
    f_bounds = bounds;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.TYPE_PARAMETER;
  }

  @Override
  @NonNull
  public List<TypeRef> getBounds() {
    return new ArrayList<TypeRef>(f_bounds);
  }
}
