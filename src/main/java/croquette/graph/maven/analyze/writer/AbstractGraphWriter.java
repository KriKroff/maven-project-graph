package croquette.graph.maven.analyze.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;

import croquette.graph.maven.analyze.analysis.ClassAnalysis;
import croquette.graph.maven.analyze.analysis.ClassIdentifier;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

public abstract class AbstractGraphWriter implements GraphWriter {

  @Override
  public void writeGraph(MavenProject project, ProjectDependencyAnalysis projectAnalysis, ArtifactFilter expandFilter)
      throws IOException {
    String directory = project.getBuild().getOutputDirectory();
    File classesDirectory = new File(directory);
    File outputFile = new File(classesDirectory.getParentFile(), getFileName());

    Path outputFilePath = outputFile.toPath();
    Files.createDirectories(outputFilePath.getParent());

    Map<String, Node> nodes = new HashMap<String, Node>();
    Map<String, Edge> edges = new HashMap<String, Edge>();

    for (ClassAnalysis classAnalysis : projectAnalysis.getClassDependencies().values()) {
      addNodeAndEdges(nodes, edges, expandFilter, classAnalysis);
    }

    try (Writer writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
      writeGraph(writer, project, nodes, edges);
    }
  }

  protected abstract void writeGraph(Writer writer, MavenProject project, Map<String, Node> nodes,
      Map<String, Edge> edges) throws IOException;

  protected abstract String getFileName();

  private void addNodeAndEdges(Map<String, Node> nodes, Map<String, Edge> edges, ArtifactFilter expandFilter,
      ClassAnalysis classAnalysis) {
    if (expandFilter.include(classAnalysis.getArtifact().getArtifact())) {
      Node defaultNode = createNode(classAnalysis, true);
      String nodeId = defaultNode.getId();
      addNode(nodes, defaultNode);
      for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifact().getArtifact())) {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, true));
        } else {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, false));
        }
      }
    } else {

      Node artifactNode = createNode(classAnalysis, false);
      addNode(nodes, artifactNode);
      String nodeId = artifactNode.getId();

      for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifact().getArtifact())) {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, true));
        } else {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, false));
        }
      }
    }
  }

  private void addNode(Map<String, Node> nodes, Node node) {
    Node previousNode = nodes.get(node.getId());
    if (previousNode == null) {
      nodes.put(node.getId(), node);
    } else {
      node = previousNode;
    }

    nodeAdded(node, previousNode != null);
  }

  private void addEdge(Map<String, Edge> edges, Edge edge) {
    if (edge != null) {
      String edgeId = edge.getSourceId() + "|" + edge.getTargetId();
      Edge previousEdge = edges.get(edgeId);
      if (previousEdge == null) {
        edges.put(edgeId, edge);
      } else {
        edge = previousEdge;
      }
      edgeAdded(edge, previousEdge != null);
    }
  }

  protected abstract void nodeAdded(Node node, boolean first);

  protected abstract void edgeAdded(Edge edge, boolean first);

  protected abstract Node createNode(ClassIdentifier classAnalysis, boolean withClass);

  protected abstract Edge createEdge(ClassIdentifier source, String sourceId, ClassIdentifier target, boolean withClass);

  protected String buildNodeId(ClassIdentifier classAnalysis, boolean withClass) {
    if (withClass) {
      return classAnalysis.getArtifact().getIdentifier() + "_" + classAnalysis.getClassName();
    } else {
      return classAnalysis.getArtifact().getIdentifier();
    }
  }
}
