package croquette.graph.maven.analyze.graph;

public interface Edge {

  String getSourceId();

  String getTargetId();

  String getLabel();

  void setLabel(String label);

  Integer getWeight();

  void setWeight(int weight);
}
