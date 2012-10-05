package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.common.i18n.I18N;

@Immutable
final class DeclField extends DeclVisibility {

  @NonNull
  final IDecl f_typeOf;
  final boolean f_isStatic;
  final boolean f_isFinal;

  DeclField(IDecl parent, String name, Visibility visibility, IDecl typeOf, boolean isStatic, boolean isFinal) {
    super(parent, name, visibility);
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
  public IDecl getTypeOf() {
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
    b.append(f_typeOf);
    return b.toString();
  }
}
