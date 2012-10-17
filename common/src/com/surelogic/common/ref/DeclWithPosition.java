package com.surelogic.common.ref;

import java.util.List;

import com.surelogic.Immutable;
import com.surelogic.ValueObject;
import com.surelogic.common.i18n.I18N;

@Immutable
@ValueObject
abstract class DeclWithPosition extends Decl {

  /**
   * Between 0 and 254.
   */
  final int f_position;

  public DeclWithPosition(IDecl parent, List<DeclBuilder> childBuilders, String name, int position) {
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
