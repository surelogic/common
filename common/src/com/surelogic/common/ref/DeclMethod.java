package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.Nullable;

@Immutable
final class DeclMethod extends DeclHasFormalParameters {

  @NonNull
  final IDecl f_returnTypeOf;
  @NonNull
  final String f_formalTypeParameters;
  final boolean f_isStatic;
  final boolean f_isFinal;
  final boolean f_isAbstract;

  DeclMethod(IDecl parent, String name, Visibility visibility, IDecl[] formalParameterTypes, IDecl returnTypeOf,
      String formalTypeParameters, boolean isStatic, boolean isFinal, boolean isAbstract) {
    super(parent, name, visibility, formalParameterTypes);
    f_returnTypeOf = returnTypeOf;
    f_formalTypeParameters = formalTypeParameters == null ? "" : formalTypeParameters;
    f_isStatic = isStatic;
    f_isFinal = isFinal;
    f_isAbstract = isAbstract;
  }

  @NonNull
  public Kind getKind() {
    return Kind.METHOD;
  }

  @Override
  @NonNull
  public String getFormalTypeParameters() {
    return f_formalTypeParameters;
  }

  @Override
  @Nullable
  public IDecl getTypeOf() {
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
  String toStringHelper() {
    final StringBuilder b = new StringBuilder("#");
    b.append(f_formalTypeParameters);
    b.append(f_name);
    b.append("(");
    boolean first = true;
    for (IDecl decl : f_formalParameterTypes) {
      if (first) {
        first = false;
      } else {
        b.append(",");
      }
      b.append(decl);
    }
    b.append("):");
    b.append(f_returnTypeOf);
    return b.toString();
  }
}
