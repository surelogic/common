package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

@Immutable
@ValueObject
final class DeclField extends DeclVisibility implements IDeclField {

  @NonNull
  final TypeRef f_typeOf;
  final boolean f_isStatic;
  final boolean f_isFinal;

  DeclField(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility, TypeRef typeOf,
      boolean isStatic, boolean isFinal) {
    super(parent, childBuilders, name, visibility);
    if (typeOf == null)
      throw new IllegalArgumentException(I18N.err(44, "typeOf"));
    f_typeOf = typeOf;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
  }

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
  String toStringHelper() {
    final StringBuilder b = new StringBuilder("#");
    b.append(f_name);
    b.append(":");
    b.append(f_typeOf.getFullyQualified());
    return b.toString();
  }
}
