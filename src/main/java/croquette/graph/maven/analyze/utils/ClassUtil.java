package croquette.graph.maven.analyze.utils;

import java.util.Map;
import java.util.Set;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;

public abstract class ClassUtil {
  public static final ArtifactIdentifier findArtifactForClassName(
      Map<ArtifactIdentifier, Set<String>> artifactClassMap, String className) {
    for (Map.Entry<ArtifactIdentifier, Set<String>> entry : artifactClassMap.entrySet()) {
      if (entry.getValue().contains(className)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public static final String normalizeClassName(String className) {
    return className != null ? className.replace('/', '.').replaceAll("\\$.*", "") : null;
  }
}
