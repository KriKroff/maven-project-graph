package croquette.graph.maven.analyze.analyzer.visitor.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;

public class RemapperDependencyClassFileVisitor implements IDependencyClassFileVisitor {

  protected HashMap<String, Set<String>> collectors = new HashMap<String, Set<String>>();

  private ArtifactIdentifier artifactIdentifier;

  public RemapperDependencyClassFileVisitor(Artifact artifact) {
    this.artifactIdentifier = new ArtifactIdentifier(artifact);
  }

  public void visitClass(String className, InputStream in) {

    try {
      Set<String> classNames = null;
      try {
        classNames = ClassDependencyCollector.getClassesUsedBy(className);
      } catch (IOException e) {
        classNames = ClassDependencyCollector.getClassesUsedBy(in);
      }

      collectors.put(className, classNames);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (IndexOutOfBoundsException e) {
      System.out.println("Unable to process: " + className);
    }
  }

  @Override
  public Map<String, SimpleEntryAnalysis> getDependencies() {
    return Maps.transformEntries(collectors, new EntryTransformer<String, Set, SimpleEntryAnalysis>() {
      @Override
      public SimpleEntryAnalysis transformEntry(String key, Set value) {
        return new SimpleEntryAnalysis(artifactIdentifier, key, value);
      }
    });
  }
}
