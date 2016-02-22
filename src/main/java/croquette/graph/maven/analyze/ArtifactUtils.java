package croquette.graph.maven.analyze;

import org.apache.maven.artifact.Artifact;

import com.google.common.base.Joiner;

public abstract class ArtifactUtils {

  private static Joiner getJoiner() {
    return Joiner.on(';').useForNull("");
  }

  public static String versionLess(Artifact artifact) {
    return getJoiner().join(artifact.getGroupId(), artifact.getArtifactId());
  }

  public static String withVersion(Artifact artifact) {
    return getJoiner().join(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

}
