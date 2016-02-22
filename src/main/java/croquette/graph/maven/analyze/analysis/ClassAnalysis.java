package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class ClassAnalysis extends ClassIdentifier {

  private Set<ClassIdentifier> dependencies = new HashSet<ClassIdentifier>();

  public ClassAnalysis(String artifactIdentifier, String className, Set<ClassAnalysis> dependencies) {
    super(artifactIdentifier, className);
  }

  public ClassAnalysis(String artifactIdentifier, String className) {
	  super(artifactIdentifier,className);
  }

  public Set<ClassIdentifier> getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return new StringBuilder(this.artifactIdentifier).append("/").append(this.className).toString();
  }
}
