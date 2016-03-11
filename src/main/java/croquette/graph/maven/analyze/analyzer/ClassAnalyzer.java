package croquette.graph.maven.analyze.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.maven.shared.dependency.analyzer.ClassFileVisitorUtils;
import org.apache.maven.shared.dependency.analyzer.CollectorClassFileVisitor;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = ProjectBuildAnalyzer.class, hint = "class")
public class ClassAnalyzer implements ProjectBuildAnalyzer {

  public Set<String> analyze(URL url) throws IOException {
    CollectorClassFileVisitor visitor = new CollectorClassFileVisitor();
    try {
      ClassFileVisitorUtils.accept(url, visitor);
    } catch (ZipException e) {
      ZipException ze = new ZipException("Cannot process Jar entry on URL: " + url + " due to " + e.getMessage());
      ze.initCause(e);
      throw ze;
    }

    return visitor.getClasses();
  }
}
