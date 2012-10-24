package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclInterface extends DeclVisibility implements IDeclType {

  DeclInterface(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }
}
