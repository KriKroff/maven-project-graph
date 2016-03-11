package croquette.graph.maven.analyze.writer.dot;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.maven.project.MavenProject;

import croquette.graph.maven.analyze.analysis.JarEntryDescription;
import croquette.graph.maven.analyze.graph.AbstractGraphWriter;
import croquette.graph.maven.analyze.graph.Edge;
import croquette.graph.maven.analyze.graph.Node;

public class DotGraphWriter extends AbstractGraphWriter {

  private void writeNode(Writer writer, Node node) throws IOException {
    StringBuilder sb = new StringBuilder("\t" + DotEscaper.escape(node.getId()));
    if (node.getLabel() != null || node.getWeight() != null) {
      sb.append(" [");
      if (node.getLabel() != null) {
        sb.append("label=\"").append(node.getLabel()).append("\" ");
      }

      if (node.getWeight() != null) {
        sb.append("weight=\"").append(node.getWeight()).append("\" ");
      }

      sb.append("]");
    }
    sb.append("\n");
    writer.write(sb.toString());
  }

  private void writeEdge(Writer writer, Edge edge) throws IOException {
    StringBuilder sb = new StringBuilder("\t" + DotEscaper.escape(edge.getSourceId()));
    sb.append(" -> ").append(DotEscaper.escape(edge.getTargetId()));
    if (edge.getLabel() != null || edge.getWeight() != null) {
      sb.append(" [");
      if (edge.getLabel() != null) {
        sb.append("label=\"").append(edge.getLabel()).append("\" ");
      }

      if (edge.getWeight() != null) {
        sb.append("weight=\"").append(edge.getWeight()).append("\" ");
      }

      sb.append("]");
    }
    sb.append("\n");
    writer.write(sb.toString());
  }

  @Override
  protected void writeGraph(Writer writer, MavenProject project, Map<String, Node> nodes, Map<String, Edge> edges)
      throws IOException {

    writer.write("digraph " + project.getId() + " {\n");

    for (Node node : nodes.values()) {
      writeNode(writer, node);
    }
    writer.write("//EDGES\n");
    for (Edge edge : edges.values()) {
      writeEdge(writer, edge);
    }

    writer.write("}");
  }

  @Override
  protected String getFileName() {
    return "dependencyGraph.dot";
  }

  @Override
  protected void nodeAdded(Node node, boolean first) {
    int weight = node.getWeight() != null ? node.getWeight() : 0;
    weight++;
    node.setWeight(weight);
  }

  @Override
  protected void edgeAdded(Edge edge, boolean first) {
    int weight = edge.getWeight() != null ? edge.getWeight() : 0;
    weight++;
    edge.setWeight(weight);
  }

  @Override
  protected Node createNode(JarEntryDescription classAnalysis, boolean withClass) {
    return new DotNode(buildNodeId(classAnalysis, withClass));
  }

  @Override
  protected Edge createEdge(JarEntryDescription source, String sourceId, JarEntryDescription target, boolean withClass) {
    String targetId = buildNodeId(target, withClass);
    if (!sourceId.equals(targetId)) {
      System.out.println(sourceId + " " + targetId);
      return new DotEdge(sourceId, targetId);
    }
    return null;
  }
}
