package croquette.graph.maven.analyze.analysis;

import com.google.common.base.Objects;

public class ClassIdentifier {

  protected final String className;

  protected final String artifactIdentifier;

  public ClassIdentifier(String artifactIdentifier, String className) {
    this.artifactIdentifier = artifactIdentifier;
    this.className = className;
  }

  public String getArtifactIdentifier() {
    return artifactIdentifier;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ClassIdentifier) {
      return Objects.equal(((ClassIdentifier) obj).getClassName(), getClassName());
    }
    return false;
  }

  @Override
  public String toString() {
    return new StringBuilder(this.artifactIdentifier).append("/").append(this.className).toString();
  }
}
