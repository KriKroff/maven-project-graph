package croquette.graph.maven.analyze.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.ClassFileVisitorUtils;
import org.codehaus.plexus.component.annotations.Component;

import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;
import croquette.graph.maven.analyze.analyzer.asm.DependencyClassFileVisitor;
import croquette.graph.maven.analyze.analyzer.asm.RemapperDependencyClassFileVisitor;

@Component(role = InternalClassDependencyAnalyzer.class)
public class ASMDependencyAnalyzer implements InternalClassDependencyAnalyzer {
  // DependencyAnalyzer methods ---------------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.DependencyAnalyzer#analyze(java.net.URL)
   */
  public Map<String, InternalClassAnalysis> analyze(Artifact artifact, URL url) throws IOException {
    DependencyClassFileVisitor visitor = new RemapperDependencyClassFileVisitor(artifact);
    ClassFileVisitorUtils.accept(url, visitor);
    Map<String, InternalClassAnalysis> dependencies = visitor.getDependencies();
    return dependencies;
  }
}
