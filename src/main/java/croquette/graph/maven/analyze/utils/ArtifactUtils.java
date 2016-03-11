package croquette.graph.maven.analyze.utils;

import org.apache.maven.artifact.Artifact;

import com.google.common.base.Joiner;

public abstract class ArtifactUtils {

  private static final Joiner getJoiner() {
    return Joiner.on(':').useForNull("");
  }

  public static final String versionLess(Artifact artifact) {
    return getJoiner().join(artifact.getGroupId(), artifact.getArtifactId());
  }

  public static final String withVersion(Artifact artifact) {
    return getJoiner().join(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

}
