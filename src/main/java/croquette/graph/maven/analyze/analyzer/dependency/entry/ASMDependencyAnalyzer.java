package croquette.graph.maven.analyze.analyzer.dependency.entry;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.ClassFileVisitorUtils;
import org.codehaus.plexus.component.annotations.Component;

import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;
import croquette.graph.maven.analyze.analyzer.visitor.asm.IDependencyClassFileVisitor;
import croquette.graph.maven.analyze.analyzer.visitor.asm.RemapperDependencyClassFileVisitor;

@Component(role = IInternalEntryDependencyAnalyzer.class, hint = "class")
public class ASMDependencyAnalyzer implements IInternalEntryDependencyAnalyzer {
  // DependencyAnalyzer methods ---------------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.DependencyAnalyzer#analyze(java.net.URL)
   */
  public Map<String, SimpleEntryAnalysis> analyze(Artifact artifact, URL url) throws IOException {
    IDependencyClassFileVisitor visitor = new RemapperDependencyClassFileVisitor(artifact);
    ClassFileVisitorUtils.accept(url, visitor);
    Map<String, SimpleEntryAnalysis> dependencies = visitor.getDependencies();
    return dependencies;
  }
}
