package croquette.graph.maven.analyze.analysis;

import com.google.common.base.Objects;

public class ClassIdentifier {

  protected final String className;

  protected final ArtifactIdentifier artifact;

  public ClassIdentifier(ArtifactIdentifier artifact, String className) {
    this.artifact = artifact;
    this.className = className;
  }

  public ArtifactIdentifier getArtifact() {
    return artifact;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ClassIdentifier) {
      return Objects.equal(artifact, ((ClassIdentifier) obj).getArtifact())
          && Objects.equal(((ClassIdentifier) obj).getClassName(), getClassName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.className.hashCode();
  }

  @Override
  public String toString() {
    String artifactString = this.artifact != null ? this.artifact.toString() : "";
    return new StringBuilder(artifactString).append("/").append(this.className).toString();
  }
}
