package croquette.graph.maven.analyze.writer.dot;

import croquette.graph.maven.analyze.graph.DefaultNode;

public class DotNode extends DefaultNode {

  public DotNode(String id) {
    this(id, null);
  }

  public DotNode(String id, String label) {
    super(id, label);
  }
}
