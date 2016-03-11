package croquette.graph.maven.analyze.analyzer.dependency.entry;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.component.annotations.Component;

import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;
import croquette.graph.maven.analyze.analyzer.XmlFileVisitorUtils;
import croquette.graph.maven.analyze.analyzer.visitor.SpringXmlFileVisitor;

@Component(role = IInternalEntryDependencyAnalyzer.class, hint = "xml")
public class XmlDependencyAnalyzer implements IInternalEntryDependencyAnalyzer {
  // DependencyAnalyzer methods ---------------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.DependencyAnalyzer#analyze(java.net.URL)
   */
  public Map<String, SimpleEntryAnalysis> analyze(Artifact artifact, URL url) throws IOException {
    SpringXmlFileVisitor visitor = new SpringXmlFileVisitor(artifact);
    XmlFileVisitorUtils.accept(url, visitor);
    Map<String, SimpleEntryAnalysis> dependencies = visitor.getXmlDependencies();
    return dependencies;
  }
}
