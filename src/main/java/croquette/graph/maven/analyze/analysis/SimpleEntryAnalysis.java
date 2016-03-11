package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class SimpleEntryAnalysis extends JarEntryDescription {

  private Set<String> dependencies = new HashSet<String>();

  public SimpleEntryAnalysis(ArtifactIdentifier artifact, String objectName, Set<String> dependencies) {
    super(artifact, objectName);
    this.dependencies = dependencies;
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

}
