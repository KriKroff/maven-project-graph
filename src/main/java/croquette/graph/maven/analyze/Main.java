package croquette.graph.maven.analyze;

import java.io.IOException;
import java.util.Collection;

import croquette.graph.maven.analyze.analyzer.asm.Collector;
import croquette.graph.maven.analyze.writer.dot.DotGraphWriter;

public class Main {

  public static void main(String[] args) throws IOException {
    final Collection<String> classes = Collector.getClassesUsedBy(DotGraphWriter.class.getName());

    System.out.println("Used classes:");
    for (String cls : classes) {
      System.out.println(" - " + cls);
    }
  }

}
