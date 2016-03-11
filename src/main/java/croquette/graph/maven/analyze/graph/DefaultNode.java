package croquette.graph.maven.analyze.graph;

public class DefaultNode implements Node {

  private final String id;
  private String label;

  private Integer weight = null;

  public DefaultNode(String id) {
    this.id = id;
  }

  public DefaultNode(String id, String label) {
    this.id = id;
    this.label = label;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public Integer getWeight() {
    return this.weight;
  }

  @Override
  public void setWeight(int weight) {
    this.weight = weight;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Node) {
      return this.id.equals(((Node) obj).getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
}
