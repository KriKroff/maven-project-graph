package croquette.graph.maven.analyze.writer;


public class DefaultEdge implements Edge {

  private final String sourceId;
  private final String targetId;

  private String label;

  private Integer weight;

  public DefaultEdge(String sourceId, String targetId) {
    this.sourceId = sourceId;
    this.targetId = targetId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Integer getWeight() {
    return this.weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getSourceId() {
    return sourceId;
  }

  public String getTargetId() {
    return targetId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Edge) {
      return this.sourceId.equals(((Edge) obj).getSourceId()) && this.targetId.equals(((Edge) obj).getTargetId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.sourceId.hashCode() + this.targetId.hashCode();
  }

}
