package croquette.graph.maven.analyze.graph;

import java.io.IOException;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

public interface GraphWriter {

  void writeGraph(MavenProject project, ProjectDependencyAnalysis projectAnalysis, ArtifactFilter expandFilter)
      throws IOException;

}