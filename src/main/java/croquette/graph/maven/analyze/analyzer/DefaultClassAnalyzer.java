package croquette.graph.maven.analyze.analyzer;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.maven.shared.dependency.analyzer.ClassAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ClassFileVisitorUtils;
import org.apache.maven.shared.dependency.analyzer.CollectorClassFileVisitor;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = ClassAnalyzer.class, hint = "class")
public class DefaultClassAnalyzer implements ClassAnalyzer {
  // ClassAnalyzer methods --------------------------------------------------

  public Set<String> analyze(URL url) throws IOException {
    CollectorClassFileVisitor visitor = new CollectorClassFileVisitor();
    System.out.println("Visiting " + url);
    try {
      ClassFileVisitorUtils.accept(url, visitor);
    } catch (ZipException e) {
      // since the current ZipException gives no indication what jar file is corrupted
      // we prefer to wrap another ZipException for better error visibility
      ZipException ze = new ZipException("Cannot process Jar entry on URL: " + url + " due to " + e.getMessage());
      ze.initCause(e);
      throw ze;
    }

    return visitor.getClasses();
  }
}
