package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.*;
import com.surelogic.common.i18n.I18N;

@Immutable
abstract class DeclVisibility extends Decl {

  @NonNull
  final Visibility f_visibility;

  DeclVisibility(IDecl parent, List<Decl.DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name);
    if (visibility == null)
      throw new IllegalArgumentException(I18N.err(44, "visibility"));
    f_visibility = visibility;
  }

  @Override
  @NonNull
  public Visibility getVisibility() {
    return f_visibility;
  }
}
