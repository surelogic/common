package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
public final class DeclInterface extends DeclType {

  DeclInterface(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }

  @NonNull
  public Kind getKind() {
    return Kind.INTERFACE;
  }

  @Override
  String toStringHelper() {
    return "." + f_name + Decl.toStringHelperTypeParameters(this);
  }
}
