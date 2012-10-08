package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclConstructor extends DeclVisibility {

  DeclConstructor(IDecl parent, Set<Decl.DeclBuilder> childBuilders, Visibility visibility) {
    super(parent, childBuilders, parent.getName(), visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.CONSTRUCTOR;
  }

  @Override
  String toStringHelper() {
    return "#" + f_name + Decl.toStringParameters(this);
  }
}
