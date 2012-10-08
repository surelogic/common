package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclInterface extends DeclVisibility {

  DeclInterface(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }

  @Override
  @NonNull
  public IDecl[] getTypeParameters() {
    return null; // TODO
  }

  @Override
  String toStringHelper() {
    return "." + f_name; // TODO
  }
}
