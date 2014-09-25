package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;

@Immutable
final class DeclMethod extends DeclVisibility implements IDeclFunction {

  /**
   * {@code null} indicates <tt>void</tt>.
   */
  @Nullable
  final TypeRef f_returnTypeOf;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;
  final boolean f_isImplicit;
  final boolean f_isDefault;

  DeclMethod(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, TypeRef returnTypeOf,
      boolean isStatic, boolean isFinal, boolean isAbstract, boolean isImplicit, boolean isDefault) {
    super(parent, childBuilders, name, visibility);
    f_returnTypeOf = returnTypeOf;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
    f_isImplicit = isImplicit;
    f_isDefault = isDefault;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.METHOD;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_returnTypeOf;
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
  public boolean isImplicit() {
    return f_isImplicit;
  }

  @Override
  public boolean isDefault() {
    return f_isDefault;
  }
}
