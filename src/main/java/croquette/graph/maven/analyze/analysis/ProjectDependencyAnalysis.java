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
  private final Map<String, JarEntryAnalysis> dependencies;

  private final Map<ArtifactIdentifier, Set<String>> artifactsClassMap;

  public ProjectDependencyAnalysis(Artifact artifact) {
    this.artifact = artifact;
    this.dependencies = new HashMap<String, JarEntryAnalysis>();
    this.artifactsClassMap = new HashMap<ArtifactIdentifier, Set<String>>();

  }

  public ProjectDependencyAnalysis(Artifact artifact, Map<ArtifactIdentifier, Set<String>> artifactClassMap,
      Map<String, JarEntryAnalysis> classDependencies) {
    this.artifact = artifact;
    this.artifactsClassMap = artifactClassMap;
    this.dependencies = classDependencies;
  }

  public Map<String, JarEntryAnalysis> getDependencies() {
    return dependencies;
  }

  public Map<ArtifactIdentifier, Set<String>> getArtifactsClassMap() {
    return artifactsClassMap;
  }

  public Artifact getArtifact() {
    return artifact;
  }

}
