package croquette.graph.maven.analyze.analyzer;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

public interface ProjectDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  String ROLE = ProjectDependencyAnalyzer.class.getName();

  // public methods ---------------------------------------------------------

  ProjectDependencyAnalysis analyze(MavenProject project) throws ProjectDependencyAnalyzerException;
}