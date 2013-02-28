package com.surelogic.common.graph;

import com.surelogic.NonNull;
import com.surelogic.ThreadSafe;
import com.surelogic.common.i18n.I18N;

@ThreadSafe
public class Node extends Element {

  Node(Graph owner, String label) {
    super(owner);
    if (label == null)
      throw new IllegalArgumentException(I18N.err(44, "label"));
    f_label = label;
  }

  @NonNull
  private final String f_label;

  @NonNull
  public String getLabel() {
    return f_label;
  }

  /**
   * {@code true} if the position of this node is fixed, {@code false}
   * otherwise.
   */
  boolean f_fixedPosition = false;

  /**
   * Gets if the position of this node is fixed.
   * 
   * @return {@code true} if the position of this node is fixed, {@code false}
   *         otherwise.
   */
  public boolean isPositionFixed() {
    synchronized (f_graph) {
      return f_fixedPosition;
    }
  }

  public void setPostitionFixed(boolean value) {
    synchronized (f_graph) {
      f_fixedPosition = value;
    }
  }

  /**
   * The <i>x</i> position of this node.
   */
  double f_x;

  public double getX() {
    synchronized (f_graph) {
      return f_x;
    }
  }

  public void setX(double value) {
    synchronized (f_graph) {
      f_x = value;
    }
  }

  /**
   * The <i>y</i> position of this node.
   */
  double f_y;

  public double getY() {
    synchronized (f_graph) {
      return f_y;
    }
  }

  public void setY(double value) {
    synchronized (f_graph) {
      f_y = value;
    }
  }

  public void setPosition(double x, double y) {
    synchronized (f_graph) {
      setX(x);
      setY(y);
    }
  }

  public void movePosition(double dx, double dy) {
    synchronized (f_graph) {
      setX(getX() + dx);
      setY(getY() + dy);
    }
  }

  /**
   * For {@link Graph#relax()}
   */
  double f_dx, f_dy;
}
