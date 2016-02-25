package croquette.graph.maven.analyze;

import java.io.IOException;
import java.util.Collection;

import croquette.graph.maven.analyze.analyzer.asm.DependencyCollector;

public class Main {

  public static void main(String[] args) throws IOException {
    final Collection<String> classes = DependencyCollector.getClassesUsedBy(DependencyCollector.class.getName());

    System.out.println("Used classes:");
    for (String cls : classes) {
      System.out.println(" - " + cls);
    }
  }

}
