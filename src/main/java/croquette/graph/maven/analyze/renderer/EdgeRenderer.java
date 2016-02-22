package croquette.graph.maven.analyze.renderer;

public interface EdgeRenderer {
  String createEdgeAttributes(Node from, Node to);
}
