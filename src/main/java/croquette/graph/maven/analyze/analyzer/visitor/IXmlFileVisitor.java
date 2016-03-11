package croquette.graph.maven.analyze.analyzer.visitor;

import java.io.InputStream;

public interface IXmlFileVisitor {
  void visitFile(String fileName, InputStream in);

}
