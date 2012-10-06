package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.common.SLUtility;

@Immutable
final class DeclPackage extends Decl {

  DeclPackage(Set<Decl.DeclBuilder> childBuilders) {
    super(null, childBuilders, SLUtility.JAVA_DEFAULT_PACKAGE);
  }

  DeclPackage(IDecl parent, Set<Decl.DeclBuilder> childBuilders, String name) {
    super(parent, childBuilders, name);
  }

  @NonNull
  public Kind getKind() {
    return Kind.PACKAGE;
  }

  @Override
  String toStringHelper() {
    return f_parent == null ? f_name : "." + f_name;
  }
}
