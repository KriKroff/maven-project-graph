package croquette.graph.maven.analyze.writer.dot;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;

import croquette.graph.maven.analyze.analysis.ClassAnalysis;
import croquette.graph.maven.analyze.analysis.ClassIdentifier;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.writer.Edge;
import croquette.graph.maven.analyze.writer.GraphWriter;
import croquette.graph.maven.analyze.writer.Node;

public class DotGraphWriter implements GraphWriter {

  /*
   * (non-Javadoc)
   * 
   * @see croquette.graph.maven.analyze.writer.dot.GraphWriter#writeGraph(org.apache.maven.project.MavenProject,
   * croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis,
   * org.apache.maven.artifact.resolver.filter.ArtifactFilter)
   */
  @Override
  public void writeGraph(MavenProject project, ProjectDependencyAnalysis projectAnalysis, ArtifactFilter expandFilter)
      throws IOException {
    writeDotFile(project, projectAnalysis, expandFilter);
  }

  protected void writeDotFile(MavenProject project, ProjectDependencyAnalysis projectAnalysis,
      ArtifactFilter expandFilter) throws IOException {
    String directory = project.getBuild().getOutputDirectory();
    File classesDirectory = new File(directory);
    File outputFile = new File(classesDirectory.getParentFile(), "dependencyGraph.dot");

    Path outputFilePath = outputFile.toPath();
    Files.createDirectories(outputFilePath.getParent());

    Set<Node> nodes = new HashSet<Node>();
    Set<Edge> edges = new HashSet<Edge>();

    for (ClassAnalysis classAnalysis : projectAnalysis.getClassDependencies().values()) {
      addNodeAndEdges(nodes, edges, expandFilter, classAnalysis);
    }

    try (Writer writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
      writer.write("digraph " + project.getId() + " {\n");

      for (Node node : nodes) {
        writeNode(writer, node);
      }
      writer.write("//EDGES\n");
      for (Edge edge : edges) {
        writeEdge(writer, edge);
      }

      writer.write("}");

    }
  }

  private void writeNode(Writer writer, Node node) throws IOException {
    StringBuilder sb = new StringBuilder("\t" + node.getId());
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
    StringBuilder sb = new StringBuilder("\t" + edge.getSourceId());
    sb.append(" -> ").append(edge.getTargetId());
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

  private void addNodeAndEdges(Set<Node> nodes, Set<Edge> edges, ArtifactFilter expandFilter,
      ClassAnalysis classAnalysis) {
    if (expandFilter.include(classAnalysis.getArtifact().getArtifact())) {
      String nodeId = buildNodeId(classAnalysis, true);
      Node defaultNode = new DotNode(nodeId, classAnalysis.getClassName());
      nodes.add(defaultNode);
      for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifact().getArtifact())) {
          edges.add(new DotEdge(nodeId, buildNodeId(dependency, true)));
        } else {
          edges.add(new DotEdge(nodeId, buildNodeId(dependency, false)));
        }
      }
    } else {
      String nodeId = buildNodeId(classAnalysis, false);
      nodes.add(new DotNode(nodeId));
      for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifact().getArtifact())) {
          edges.add(new DotEdge(nodeId, buildNodeId(dependency, true)));
        } else {
          edges.add(new DotEdge(nodeId, buildNodeId(dependency, false)));
        }
      }
    }
  }

  private String buildNodeId(ClassIdentifier classAnalysis, boolean withClass) {
    if (withClass) {
      return classAnalysis.getArtifact().getIdentifier() + "_" + classAnalysis.getClassName();
    } else {
      return classAnalysis.getArtifact().getIdentifier();
    }
  }

}
