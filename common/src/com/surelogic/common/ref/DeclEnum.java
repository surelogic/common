package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclEnum extends DeclVisibility implements IDeclType {

  DeclEnum(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.ENUM;
  }

  @Override
  String toStringHelper() {
    return "." + f_name;
  }
}
