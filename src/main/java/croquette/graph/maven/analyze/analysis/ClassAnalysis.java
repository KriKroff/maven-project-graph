package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class ClassAnalysis extends ClassIdentifier {

  private Set<ClassIdentifier> dependencies = new HashSet<ClassIdentifier>();

  public ClassAnalysis(ArtifactIdentifier artifact, String className, Set<ClassAnalysis> dependencies) {
    super(artifact, className);
  }

  public ClassAnalysis(ArtifactIdentifier artifact, String className) {
    super(artifact, className);
  }

  public Set<ClassIdentifier> getDependencies() {
    return dependencies;
  }

}
