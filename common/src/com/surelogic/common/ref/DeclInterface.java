package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;

@Immutable
final class DeclInterface extends DeclVisibility implements IDeclType {

  DeclInterface(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }
}
