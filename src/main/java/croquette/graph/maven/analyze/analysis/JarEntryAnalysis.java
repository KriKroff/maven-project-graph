package croquette.graph.maven.analyze.analysis;

import java.util.HashSet;
import java.util.Set;

public class JarEntryAnalysis extends JarEntryDescription {

  private Set<JarEntryDescription> dependencies = new HashSet<JarEntryDescription>();

  public JarEntryAnalysis(ArtifactIdentifier artifact, String className, Set<JarEntryAnalysis> dependencies) {
    super(artifact, className);
  }

  public JarEntryAnalysis(ArtifactIdentifier artifact, String className) {
    super(artifact, className);
  }

  public Set<JarEntryDescription> getDependencies() {
    return dependencies;
  }

  public String getClassName() {
    return this.getId();
  }

}
