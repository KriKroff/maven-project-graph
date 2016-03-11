package croquette.graph.maven.analyze.graph;

public interface Node {

  String getId();

  String getLabel();

  void setLabel(String label);

  Integer getWeight();

  void setWeight(int i);
}
