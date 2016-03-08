package croquette.graph.maven.analyze.analyzer;

import java.io.InputStream;

public interface XmlFileVisitor {
  void visitFile(String fileName, InputStream in);

}
