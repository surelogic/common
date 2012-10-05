package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;

@Immutable
final class DeclConstructor extends DeclHasFormalParameters {

  DeclConstructor(IDecl parent, Visibility visibility, IDecl[] formalParameterTypes) {
    super(parent, parent.getName(), visibility, formalParameterTypes);
  }

  @NonNull
  public Kind getKind() {
    return Kind.CONSTRUCTOR;
  }

  @Override
  String toStringHelper() {
    final StringBuilder b = new StringBuilder("#");
    b.append(f_name);
    b.append("(");
    boolean first = true;
    for (IDecl decl : f_formalParameterTypes) {
      if (first) {
        first = false;
      } else {
        b.append(",");
      }
      b.append(decl.toString());
    }
    b.append(")");
    return b.toString();
  }
}
