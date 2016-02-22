package croquette.graph.maven.analyze.renderer;

import org.apache.maven.artifact.Artifact;

public interface Node {

  Artifact getArtifact();

  NodeResolution getResolution();
}
