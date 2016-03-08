package croquette.graph.maven.analyze.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

public class ProjectDependencyAnalysis {

  private final Artifact artifact;

  /**
   * Dependency map Class -> classes
   */
  private final Map<String, ClassAnalysis> classDependencies;

  private final Map<ArtifactIdentifier, Set<String>> artifactsClassMap;

  public ProjectDependencyAnalysis(Artifact artifact) {
    this.artifact = artifact;
    this.classDependencies = new HashMap<String, ClassAnalysis>();
    this.artifactsClassMap = new HashMap<ArtifactIdentifier, Set<String>>();

  }

  public ProjectDependencyAnalysis(Artifact artifact, Map<ArtifactIdentifier, Set<String>> artifactClassMap,
      Map<String, ClassAnalysis> classDependencies) {
    this.artifact = artifact;
    this.artifactsClassMap = artifactClassMap;
    this.classDependencies = classDependencies;
  }

  public Map<String, ClassAnalysis> getClassDependencies() {
    return classDependencies;
  }

  public Map<ArtifactIdentifier, Set<String>> getArtifactsClassMap() {
    return artifactsClassMap;
  }

  public Artifact getArtifact() {
    return artifact;
  }

}
