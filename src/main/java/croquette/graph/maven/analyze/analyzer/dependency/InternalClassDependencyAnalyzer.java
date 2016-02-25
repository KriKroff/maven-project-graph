package croquette.graph.maven.analyze.analyzer.dependency;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.DependencyAnalyzer;

import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;

public interface InternalClassDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  String ROLE = DependencyAnalyzer.class.getName();

  // public methods ---------------------------------------------------------

  Map<String, InternalClassAnalysis> analyze(Artifact artifact, URL url) throws IOException;
}