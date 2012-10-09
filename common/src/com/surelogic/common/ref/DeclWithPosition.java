package com.surelogic.common.ref;

import java.util.Set;

import com.surelogic.common.i18n.I18N;

public abstract class DeclWithPosition extends Decl {

  /**
   * Between 0 and 254.
   */
  final int f_position;

  public DeclWithPosition(IDecl parent, Set<DeclBuilder> childBuilders, String name, int position) {
    super(parent, childBuilders, name);
    // see http://www.javaspecialists.eu/archive/Issue059.html
    if (position < 0 || position > 254)
      throw new IllegalArgumentException(I18N.err(282, position));
    f_position = position;
  }

  @Override
  public int getPosition() {
    return f_position;
  }
}
