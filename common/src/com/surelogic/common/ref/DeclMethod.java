package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
final class DeclMethod extends DeclVisibility implements IDeclFunction {

  /**
   * {@code null} indicates <tt>void</tt>.
   */
  @Nullable
  final TypeRef f_returnTypeOf;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclMethod(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, TypeRef returnTypeOf,
      boolean isStatic, boolean isFinal, boolean isAbstract) {
    super(parent, childBuilders, name, visibility);
    f_returnTypeOf = returnTypeOf;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
  }

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
}
