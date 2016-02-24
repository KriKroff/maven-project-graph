package croquette.graph.maven.analyze.analysis;

import org.apache.maven.artifact.Artifact;

import com.google.common.base.Objects;

public class ClassIdentifier {

  protected final String className;

  protected final ArtifactIdentifier artifactIdentifier;

  public ClassIdentifier(ArtifactIdentifier artifact, String className) {
    this.artifactIdentifier = artifact;
    this.className = className;
  }

  public ArtifactIdentifier getArtifactIdentifier() {
    return this.artifactIdentifier;
  }

  public Artifact getArtifact() {
    return this.artifactIdentifier.getArtifact();
  }

  public String getClassName() {
    return this.className;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ClassIdentifier) {
      return Objects.equal(artifactIdentifier, ((ClassIdentifier) obj).getArtifactIdentifier())
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
    String artifactString = this.artifactIdentifier != null ? this.artifactIdentifier.toString() : "";
    return new StringBuilder(artifactString).append("/").append(this.className).toString();
  }
}
