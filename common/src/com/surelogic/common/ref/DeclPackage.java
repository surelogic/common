package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;
import com.surelogic.common.SLUtility;

@Immutable
final class DeclPackage extends Decl implements IDeclPackage {

  DeclPackage(List<Decl.DeclBuilder> childBuilders) {
    super(null, childBuilders, SLUtility.JAVA_DEFAULT_PACKAGE);
  }

  DeclPackage(List<Decl.DeclBuilder> childBuilders, String name) {
    super(null, childBuilders, name);
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.PACKAGE;
  }
}
