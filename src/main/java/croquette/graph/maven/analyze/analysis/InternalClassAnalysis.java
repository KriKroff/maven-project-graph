package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;

public class InternalClassAnalysis extends ClassIdentifier {

  private Set<String> dependencies = new HashSet<String>();

  public InternalClassAnalysis(String artifactIdentifier, String className, Set<String> dependencies) {
    super(artifactIdentifier, className);
    this.dependencies = dependencies;
  }

  public InternalClassAnalysis(String artifactIdentifier, String className) {
    super(artifactIdentifier, className);
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof InternalClassAnalysis) {
      return Objects.equal(((InternalClassAnalysis) obj).getClassName(), getClassName());
    }
    return false;
  }

}
