package croquette.graph.maven.analyze;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public class NoopArtifactFilter implements ArtifactFilter {
  @Override
  public boolean include(Artifact artifact) {
    return true;
  }

}