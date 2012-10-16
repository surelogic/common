package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.ValueObject;
import com.surelogic.common.SLUtility;

@Immutable
@ValueObject
public final class DeclPackage extends Decl {

  DeclPackage(List<Decl.DeclBuilder> childBuilders) {
    super(null, childBuilders, SLUtility.JAVA_DEFAULT_PACKAGE);
  }

  DeclPackage(List<Decl.DeclBuilder> childBuilders, String name) {
    super(null, childBuilders, name);
  }

  @NonNull
  public Kind getKind() {
    return Kind.PACKAGE;
  }

  @Override
  String toStringHelper() {
    return f_name;
  }
}
