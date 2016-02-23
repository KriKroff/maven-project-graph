package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class InternalClassAnalysis extends ClassIdentifier {

  private Set<String> dependencies = new HashSet<String>();

  public InternalClassAnalysis(ArtifactIdentifier artifact, String className, Set<String> dependencies) {
    super(artifact, className);
    this.dependencies = dependencies;
  }

  public InternalClassAnalysis(ArtifactIdentifier artifact, String className) {
    super(artifact, className);
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

}
