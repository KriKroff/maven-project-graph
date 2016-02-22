package croquette.graph.maven.analyze.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.maven.shared.dependency.analyzer.DependencyAnalyzer;

public interface ClassDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  String ROLE = DependencyAnalyzer.class.getName();

  // public methods ---------------------------------------------------------

  Map<String, Set<String>> analyze(URL url) throws IOException;
}