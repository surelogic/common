package com.surelogic.common.graph;

import com.surelogic.NonNull;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

@ThreadSafe
public abstract class Element {

  Element(Graph owner) {
    if (owner == null)
      throw new IllegalArgumentException(I18N.err(44, "owner"));
    f_graph = owner;
  }

  @NonNull
  final Graph f_graph;

  @NonNull
  public Graph getGraph() {
    return f_graph;
  }
}
