package croquette.graph.maven.analyze.renderer;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A builder to create DOT ({@linkplain http://www.graphviz.org/doc/info/lang.html}) strings by defining edges between
 * {@link Node}s. The builder allows some customizations including custom {@link NodeRenderer}s and {@link EdgeRenderer}
 * s.
 */
public final class DotBuilder {

  private NodeRenderer nodeRenderer;
  private NodeRenderer nodeLabelRenderer;
  private EdgeRenderer edgeRenderer;
  private boolean omitSelfReferences;
  private final Set<String> nodeDefinitions;
  private final Set<String> edgeDefinitions;

  public DotBuilder() {
    this.nodeLabelRenderer = DefaultRenderer.INSTANCE;
    this.nodeRenderer = DefaultRenderer.INSTANCE;
    this.edgeRenderer = DefaultRenderer.INSTANCE;

    this.nodeDefinitions = new LinkedHashSet<String>();
    this.edgeDefinitions = new LinkedHashSet<String>();
  }

  public DotBuilder useNodeRenderer(NodeRenderer nodeRenderer) {
    this.nodeRenderer = nodeRenderer;
    return this;
  }

  public DotBuilder useNodeLabelRenderer(NodeRenderer nodeLabelRenderer) {
    this.nodeLabelRenderer = nodeLabelRenderer;
    return this;
  }

  public DotBuilder useEdgeRenderer(EdgeRenderer edgeRenderer) {
    this.edgeRenderer = edgeRenderer;
    return this;
  }

  public DotBuilder omitSelfReferences() {
    this.omitSelfReferences = true;
    return this;
  }

  // no edge will be created in case one or both nodes are null.
  public DotBuilder addEdge(Node from, Node to) {
    if (from != null && to != null) {
      addNode(from);
      addNode(to);

      safelyAddEdge(from, to);
    }

    return this;
  }

  public DotBuilder addEdge(Node from, Node to, EdgeRenderer edgeRenderer) {
    EdgeRenderer originalEdgeRenderer = this.edgeRenderer;
    this.edgeRenderer = edgeRenderer;
    addEdge(from, to);
    this.edgeRenderer = originalEdgeRenderer;

    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("digraph G {").append("\n  node ")
        .append(new AttributeBuilder().shape("box").fontName("Helvetica")).append("\n  edge ")
        .append(new AttributeBuilder().fontName("Helvetica").fontSize(10));

    sb.append("\n\n  // Node Definitions:");
    for (String node : this.nodeDefinitions) {
      sb.append("\n  ").append(node);
    }

    sb.append("\n\n  // Edge Definitions:");
    for (String edge : this.edgeDefinitions) {
      sb.append("\n  ").append(edge);
    }

    return sb.append("\n}").toString();
  }

  private void addNode(Node node) {
    String nodeName = this.nodeRenderer.render(node);
    String nodeLabel = this.nodeLabelRenderer.render(node);

    String nodeDefinition = DotEscaper.escape(nodeName) + new AttributeBuilder().label(nodeLabel);
    this.nodeDefinitions.add(nodeDefinition);
  }

  private void safelyAddEdge(Node fromNode, Node toNode) {
    String fromName = this.nodeRenderer.render(fromNode);
    String toName = this.nodeRenderer.render(toNode);

    if (!this.omitSelfReferences || !fromName.equals(toName)) {
      String edgeDefinition = DotEscaper.escape(fromName) + " -> " + DotEscaper.escape(toName)
          + this.edgeRenderer.createEdgeAttributes(fromNode, toNode);
      this.edgeDefinitions.add(edgeDefinition);
    }
  }

  static enum DefaultRenderer implements EdgeRenderer, NodeRenderer {
    INSTANCE;

    @Override
    public String createEdgeAttributes(Node from, Node to) {
      return "";
    }

    @Override
    public String render(Node node) {
      return node.getArtifact().toString();
    }

  }
}
