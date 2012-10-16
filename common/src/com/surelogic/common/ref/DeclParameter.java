package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

@Immutable
@ValueObject
final class DeclParameter extends DeclWithPosition {

  @NonNull
  final TypeRef f_typeOf;
  final boolean f_isFinal;

  DeclParameter(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, int position, TypeRef typeOf, boolean isFinal) {
    super(parent, childBuilders, name, position);
    if (typeOf == null)
      throw new IllegalArgumentException(I18N.err(44, "typeOf"));
    f_typeOf = typeOf;
    f_isFinal = isFinal;
  }

  @NonNull
  public Kind getKind() {
    return Kind.PARAMETER;
  }

  @Override
  @Nullable
  public TypeRef getTypeOf() {
    return f_typeOf;
  }

  @Override
  public boolean isFinal() {
    return f_isFinal;
  }

  @Override
  String toStringHelper() {
    return f_name + ":" + f_typeOf.getFullyQualified();
  }
}
