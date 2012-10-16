package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;

@Immutable
@ValueObject
public abstract class DeclType extends DeclVisibility {
  DeclType(IDecl parent, List<DeclBuilder> childBuilders, String name, Visibility visibility) {
    super(parent, childBuilders, name, visibility);
  }
}
