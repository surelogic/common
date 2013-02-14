package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;
import com.surelogic.common.i18n.I18N;

@Immutable
final class DeclField extends DeclVisibility implements IDeclField {

  @NonNull
  final TypeRef f_typeOf;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isVolatile;

  DeclField(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, TypeRef typeOf,
      boolean isStatic, boolean isFinal, boolean isVolatile) {
    super(parent, childBuilders, name, visibility);
    if (typeOf == null)
      throw new IllegalArgumentException(I18N.err(44, "typeOf"));
    f_typeOf = typeOf;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isVolatile = isVolatile;
  }

  @Override
  @NonNull
  public Kind getKind() {
    return Kind.FIELD;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_typeOf;
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
  public boolean isVolatile() {
    return f_isVolatile;
  }
}
