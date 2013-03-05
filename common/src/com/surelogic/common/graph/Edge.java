package com.surelogic.common.graph;

import com.surelogic.NonNull;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

@ThreadSafe
public final class Edge extends Element {

  Edge(Graph owner, Node from, Node to) {
    super(owner);
    if (from == null)
      throw new IllegalArgumentException(I18N.err(44, "from"));
    f_from = from;
    if (to == null)
      throw new IllegalArgumentException(I18N.err(44, "to"));
    f_to = to;
  }

  @NonNull
  private final Node f_from;

  @NonNull
  public Node getFrom() {
    return f_from;
  }

  @NonNull
  private final Node f_to;

  @NonNull
  public Node getTo() {
    return f_to;
  }

  private double f_suggestedLength = 150.0;

  public double getSuggestedLength() {
    synchronized (f_graph) {
      return f_suggestedLength;
    }
  }

  public void setSuggestedLength(double value) {
    synchronized (f_graph) {
      f_suggestedLength = value;
    }
  }

  public double getLength() {
    final double vx, vy;
    synchronized (f_graph) {
      vx = getTo().getX() - getFrom().getX();
      vy = getTo().getY() - getFrom().getY();
    }
    double result = Math.sqrt(vx * vx + vy * vy);
    result = (result <= 0) ? .0001 : result;
    return result;
  }
}
