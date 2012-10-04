package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.common.SLUtility;

@Immutable
final class DeclPackage extends Decl {

  DeclPackage() {
    super(null, SLUtility.JAVA_DEFAULT_PACKAGE);
  }

  DeclPackage(IDecl parent, String name) {
    super(parent, name);
  }

  @NonNull
  public Kind getKind() {
    return Kind.PACKAGE;
  }
}
