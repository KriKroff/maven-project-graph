package croquette.graph.maven.analyze.analysis;

import java.util.Objects;

import org.apache.maven.artifact.Artifact;

import croquette.graph.maven.analyze.utils.ArtifactUtils;

public class ArtifactIdentifier {

  private final Artifact artifact;
  private final String identifier;

  public ArtifactIdentifier(Artifact artifact) {
    this.artifact = artifact;
    this.identifier = ArtifactUtils.versionLess(this.artifact);
  }

  public Artifact getArtifact() {
    return artifact;
  }

  public String getIdentifier() {
    return identifier;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof ArtifactIdentifier) {
      return this.identifier.equals(((ArtifactIdentifier) obj).getIdentifier());
    }
    return false;
  }

  public int hashCode() {
    return Objects.hash(this.identifier);

  };

  @Override
  public String toString() {
    return identifier;
  }

}
