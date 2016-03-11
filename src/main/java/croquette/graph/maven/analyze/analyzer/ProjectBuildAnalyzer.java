package croquette.graph.maven.analyze.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

public interface ProjectBuildAnalyzer {
  // fields -----------------------------------------------------------------

  String ROLE = ProjectBuildAnalyzer.class.getName();

  // public methods ---------------------------------------------------------

  Set<String> analyze(URL url) throws IOException;
}
