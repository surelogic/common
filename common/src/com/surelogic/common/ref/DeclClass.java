package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclClass extends DeclVisibility implements IDeclType {

  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;
  final int f_anonymousDeclPosition;
  final TypeRef f_anonymousType;

  DeclClass(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, boolean isStatic,
      boolean isFinal, boolean isAbstract, int anonymousDeclPosition, TypeRef anonymousType) {
    super(parent, childBuilders, name, visibility);
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
    f_anonymousDeclPosition = anonymousDeclPosition;
    f_anonymousType = anonymousType;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.CLASS;
  }

  @Override
  public boolean isStatic() {
    return f_isStatic;
  }

  @Override
  public boolean isFinal() {
    return f_isFinal;
  }

  @Override
  public boolean isAbstract() {
    return f_isAbstract;
  }

  @Override
  public int getPosition() {
    return f_anonymousDeclPosition;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_anonymousType;
  }
}
