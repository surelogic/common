package com.surelogic.common.ref;

import com.surelogic.Immutable;
import com.surelogic.NonNull;
import com.surelogic.common.i18n.I18N;

@Immutable
abstract class DeclVisibility extends Decl {

  @NonNull
  final Visibility f_visibility;

  DeclVisibility(IDecl parent, String name, Visibility visibility) {
    super(parent, name);
    if (visibility == null)
      throw new IllegalArgumentException(I18N.err(44, "visibility"));
    f_visibility = visibility;
  }

  @Override
  @NonNull
  public Visibility getVisiblity() {
    return f_visibility;
  }

  @Override
  String toStringHelper() {
    return "." + f_name;
  }
}
