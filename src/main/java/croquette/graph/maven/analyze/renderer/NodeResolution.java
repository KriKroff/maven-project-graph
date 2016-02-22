package croquette.graph.maven.analyze.renderer;

public enum NodeResolution {

  INCLUDED,
  OMITTED_FOR_DUPLICATE,
  OMITTED_FOR_CONFLICT,
  OMITTED_FOR_CYCLE;
}
