package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;

@Immutable
final class DeclEnum extends DeclVisibility implements IDeclType {

  DeclEnum(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.ENUM;
  }
}
