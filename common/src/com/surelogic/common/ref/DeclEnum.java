package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
public final class DeclEnum extends DeclVisibility {

  DeclEnum(IDecl parent, String name, Visibility visibility) {
    super(parent, name, visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.ENUM;
  }
}
