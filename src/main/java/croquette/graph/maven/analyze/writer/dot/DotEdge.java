package croquette.graph.maven.analyze.writer.dot;

import croquette.graph.maven.analyze.writer.DefaultEdge;

public class DotEdge extends DefaultEdge {

  public DotEdge(String sourceId, String targetId) {
    super(DotEscaper.escape(sourceId), DotEscaper.escape(targetId));
  }

}
