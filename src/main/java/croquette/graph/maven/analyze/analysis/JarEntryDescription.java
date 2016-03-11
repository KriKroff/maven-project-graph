package croquette.graph.maven.analyze.analysis;

import org.apache.maven.artifact.Artifact;

import com.google.common.base.Objects;

public class JarEntryDescription {

  protected final String id;

  protected final ArtifactIdentifier artifactIdentifier;

  public JarEntryDescription(ArtifactIdentifier artifact, String id) {
    this.artifactIdentifier = artifact;
    this.id = id;
  }

  public ArtifactIdentifier getArtifactIdentifier() {
    return this.artifactIdentifier;
  }

  public Artifact getArtifact() {
    return this.artifactIdentifier.getArtifact();
  }

  public String getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof JarEntryDescription) {
      return Objects.equal(artifactIdentifier, ((JarEntryDescription) obj).getArtifactIdentifier())
          && Objects.equal(((JarEntryDescription) obj).getId(), getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public String toString() {
    String artifactString = this.artifactIdentifier != null ? this.artifactIdentifier.toString() : "";
    return new StringBuilder(artifactString).append("/").append(this.id).toString();
  }
}
