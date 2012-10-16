package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclConstructor extends DeclVisibility {

  final boolean f_isImplicit;

  DeclConstructor(IDecl parent, List<Decl.DeclBuilder> childBuilders, Visibility visibility, boolean isImplicit) {
    super(parent, childBuilders, parent.getName(), visibility);
    f_isImplicit = isImplicit;
  }

  @NonNull
  public Kind getKind() {
    return Kind.CONSTRUCTOR;
  }

  @Override
  public boolean isImplicit() {
    return f_isImplicit;
  }

  @Override
  String toStringHelper() {
    return "#" + f_name + Decl.toStringHelperParameters(this);
  }

}
