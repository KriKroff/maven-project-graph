package croquette.graph.maven.analyze.renderer;

import org.apache.maven.artifact.Artifact;

import com.google.common.base.Joiner;

enum NodeRenderers implements NodeRenderer {
  ARTIFACT_ID_LABEL {
    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();
      return toScopedString(artifact.getArtifactId(), artifact.getScope());
    }
  },

  ARTIFACT_ID_VERSION_LABEL {

    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();
      String artifactIdAndVersion = artifact.getArtifactId() + "\n" + artifact.getVersion();

      return toScopedString(artifactIdAndVersion, artifact.getScope());
    }

  },

  GROUP_ID_LABEL {

    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();
      return toScopedString(artifact.getGroupId(), artifact.getScope());
    }

  },

  SCOPED_GROUP_ID {
    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();
      return COLON_JOINER.join(artifact.getGroupId(), artifact.getScope());
    }
  },

  SCOPED_GROUP_ARTIFACT_ID {
    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();
      return COLON_JOINER.join(artifact.getGroupId(), artifact.getArtifactId(), artifact.getScope());
    }
  },

  VERSIONLESS_ID {

    @Override
    public String render(Node node) {
      Artifact artifact = node.getArtifact();

      return COLON_JOINER.join(artifact.getGroupId(), artifact.getArtifactId(), artifact.getType(),
          artifact.getClassifier(), artifact.getScope());
    }

  };

  private static final Joiner COLON_JOINER = Joiner.on(":").useForNull("");

  private static String toScopedString(String string, String scope) {
    if (scope != null && !"compile".equals(scope)) {
      return string + "\n(" + scope + ")";
    }

    return string;
  }

}
