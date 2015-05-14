package com.surelogic.common.graph;

import java.util.ArrayList;
import java.util.Iterator;

import com.surelogic.NonNull;
import com.surelogic.NotThreadSafe;
import com.surelogic.Nullable;
import com.surelogic.ThreadSafe;
import com.surelogic.common.Pair;
import com.surelogic.common.i18n.I18N;

/**
 * A simple directed graph with labeled nodes.
 */
@ThreadSafe
public class Graph {

  @NotThreadSafe
  public static class Builder {

    final ArrayList<Pair<String, String>> f_labelEdges = new ArrayList<>();

    public void addEdge(@NonNull String fromNodeLabel, @NonNull String toNodeLabel) {
      if (fromNodeLabel == null)
        throw new IllegalArgumentException(I18N.err(44, "fromNodeLabel"));
      if (toNodeLabel == null)
        throw new IllegalArgumentException(I18N.err(44, "toNodeLabel"));
      Pair<String, String> labelEdge = new Pair<>(fromNodeLabel, toNodeLabel);
      f_labelEdges.add(labelEdge);
    }

    final ArrayList<String> f_fixedNodeLabels = new ArrayList<>();

    public void fixPostionOf(@NonNull String nodeLabel) {
      if (nodeLabel == null)
        throw new IllegalArgumentException(I18N.err(44, "nodeLabel"));
      f_fixedNodeLabels.add(nodeLabel);
    }

    public Graph build() {
      if (f_labelEdges.isEmpty())
        throw new IllegalStateException("Graph cannot be empty");
      final Graph result = new Graph();
      synchronized (result) {
        // construct graph
        for (Pair<String, String> labelEdge : f_labelEdges) {
          Node from = result.getNode(labelEdge.first());
          if (from == null) {
            from = new Node(result, labelEdge.first());
            result.f_nodes.add(from);
          }

          Node to = result.getNode(labelEdge.second());
          if (to == null) {
            to = new Node(result, labelEdge.second());
            result.f_nodes.add(to);
          }

          Edge e = result.getEdge(from, to);
          if (e == null) {
            e = new Edge(result, from, to);
            result.f_edges.add(e);
          }
        }
        // fix position of user requested nodes
        for (String nodeLabel : f_fixedNodeLabels) {
          final Node n = result.getNode(nodeLabel);
          if (n == null)
            throw new IllegalStateException("Fixed node " + nodeLabel + " is not in the graph");
          else
            n.setPostitionFixed(true);
        }
      }
      return result;
    }
  }

  Graph() {
    // only via builder
  }

  /**
   * Nodes in this graph.
   */
  final ArrayList<Node> f_nodes = new ArrayList<>();

  /**
   * Gets an iterator over the nodes in this graph.
   * <p>
   * The caller must hold a <b>lock on this graph</b> when using this iterator.
   * 
   * @return an iterator over the nodes in this graph.
   */
  public Iterable<Node> nodes() {
    return new Iterable<Node>() {
      @Override
      public Iterator<Node> iterator() {
        return f_nodes.iterator();
      }
    };
  }

  @Nullable
  public Node getNode(String label) {
    synchronized (this) {
      for (Node n : f_nodes) {
        if (n.getLabel().equals(label))
          return n;
      }
    }
    return null;
  }

  public boolean allNodesFixedPosition() {
    synchronized (this) {
      for (Node n : f_nodes) {
        if (!n.isPositionFixed())
          return false;
      }
    }
    return true;
  }

  public void transform(double dx, double dy) {
    synchronized (this) {
      for (Node n : f_nodes) {
        n.movePosition(dx, dy);
      }
    }
  }

  /**
   * Edges in this graph.
   */
  final ArrayList<Edge> f_edges = new ArrayList<>();

  /**
   * Gets an iterator over the edges in this graph.
   * <p>
   * The caller must hold a <b>lock on this graph</b> when using this iterator.
   * 
   * @return an iterator over the edges in this graph.
   */
  public Iterable<Edge> edges() {
    return new Iterable<Edge>() {
      @Override
      public Iterator<Edge> iterator() {
        return f_edges.iterator();
      }
    };
  }

  @Nullable
  public Edge getEdge(Node from, Node to) {
    synchronized (this) {
      for (Edge e : f_edges) {
        if (e.getFrom() == from && e.getTo() == to)
          return e;
      }
    }
    return null;
  }

  public void relax() {
    synchronized (this) {
      if (allNodesFixedPosition())
        return;

      for (final Edge e : f_edges) {
        double vx = e.getTo().f_x - e.getFrom().f_x;
        double vy = e.getTo().f_y - e.getFrom().f_y;
        double actualLength = e.getLength();
        double f = (e.getSuggestedLength() - actualLength) / (actualLength * 3);
        double dx = f * vx;
        double dy = f * vy;

        e.getTo().f_dx += dx;
        e.getTo().f_dy += dy;
        e.getFrom().f_dx += -dx;
        e.getFrom().f_dy += -dy;
      }

      for (final Node n1 : f_nodes) {
        double dx = 0;
        double dy = 0;
        for (final Node n2 : f_nodes) {
          if (n1 == n2)
            continue;
          double vx = n1.f_x - n2.f_x;
          double vy = n1.f_y - n2.f_y;
          double len = vx * vx + vy * vy;
          if (len == 0) {
            dx += Math.random();
            dy += Math.random();
          } else if (len < 100 * 100) {
            dx += vx / len;
            dy += vy / len;
          }
        }
        double dlen = dx * dx + dy * dy;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          n1.f_dx += dx / dlen;
          n1.f_dy += dy / dlen;
        }
      }

      for (final Node n : f_nodes) {
        if (!n.isPositionFixed()) {
          n.movePosition(Math.max(-5, Math.min(5, n.f_dx)), Math.max(-5, Math.min(5, n.f_dy)));
        }
        n.f_dx /= 2;
        n.f_dy /= 2;
      }
    }
  }
}
