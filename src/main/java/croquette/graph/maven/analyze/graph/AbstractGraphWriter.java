package croquette.graph.maven.analyze.graph;

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

import croquette.graph.maven.analyze.analysis.JarEntryAnalysis;
import croquette.graph.maven.analyze.analysis.JarEntryDescription;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.utils.ClassUtil;

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

    for (JarEntryAnalysis classAnalysis : projectAnalysis.getDependencies().values()) {
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
      JarEntryAnalysis classAnalysis) {
    if (expandFilter.include(classAnalysis.getArtifactIdentifier().getArtifact())) {
      Node defaultNode = createNode(classAnalysis, true);
      String nodeId = defaultNode.getId();
      addNode(nodes, defaultNode);
      for (JarEntryDescription dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifactIdentifier().getArtifact())) {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, true));
        } else {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, false));
        }
      }
    } else {

      Node artifactNode = createNode(classAnalysis, false);
      addNode(nodes, artifactNode);
      String nodeId = artifactNode.getId();

      for (JarEntryDescription dependency : classAnalysis.getDependencies()) {
        if (expandFilter.include(dependency.getArtifactIdentifier().getArtifact())) {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, true));
        } else {
          addEdge(edges, createEdge(classAnalysis, nodeId, dependency, false));
        }
      }
    }
  }

  private void addNode(Map<String, Node> nodes, Node node) {
    Node previousNode = nodes.get(ClassUtil.normalizeClassName(node.getId()));
    if (previousNode == null) {
      nodes.put(ClassUtil.normalizeClassName(node.getId()), node);
    } else {
      node = previousNode;
    }

    nodeAdded(node, previousNode != null);
  }

  private void addEdge(Map<String, Edge> edges, Edge edge) {
    if (edge != null) {
      String edgeId = ClassUtil.normalizeClassName(edge.getSourceId()) + "|"
          + ClassUtil.normalizeClassName(edge.getTargetId());
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

  protected abstract Node createNode(JarEntryDescription classAnalysis, boolean withClass);

  protected abstract Edge createEdge(JarEntryDescription source, String sourceId, JarEntryDescription target,
      boolean withClass);

  protected String buildNodeId(JarEntryDescription classAnalysis, boolean withClass) {
    if (withClass) {
      return classAnalysis.getArtifactIdentifier().getIdentifier() + "_" + classAnalysis.getId();
    } else {
      return classAnalysis.getArtifactIdentifier().getIdentifier();
    }
  }
}
