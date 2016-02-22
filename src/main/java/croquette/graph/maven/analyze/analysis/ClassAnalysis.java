package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class ClassAnalysis extends ClassIdentifier {

  private Set<ClassIdentifier> dependencies = new HashSet<ClassIdentifier>();

  public ClassAnalysis(String className, Set<ClassAnalysis> dependencies) {
    super(className, className);
  }

  public ClassAnalysis(String className) {
    this.className = className;
  }

  public Set<ClassIdentifier> getDependencies() {
    return dependencies;
  }

  @Override
  public String toString() {
    return new StringBuilder(this.artifactIdentifier).append("/").append(this.className).toString();
  }
}
