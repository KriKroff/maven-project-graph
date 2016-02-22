package croquette.graph.maven.analyze.renderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AttributeBuilder {

  private final Map<String, String> attributes;

  public AttributeBuilder() {
    this.attributes = new LinkedHashMap<String, String>();
  }

  public AttributeBuilder label(String label) {
    return addAttribute("label", label);
  }

  public AttributeBuilder fontName(String fontName) {
    return addAttribute("fontname", fontName);
  }

  public AttributeBuilder fontSize(int fontSize) {
    return addAttribute("fontsize", Integer.toString(fontSize));
  }

  public AttributeBuilder fontColor(String color) {
    return addAttribute("fontcolor", color);
  }

  public AttributeBuilder style(String style) {
    return addAttribute("style", style);
  }

  public AttributeBuilder color(String color) {
    return addAttribute("color", color);
  }

  public AttributeBuilder shape(String shape) {
    return addAttribute("shape", shape);
  }

  public AttributeBuilder addAttribute(String key, String value) {
    this.attributes.put(key, DotEscaper.escape(value));
    return this;
  }

  @Override
  public String toString() {
    if (this.attributes.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder("[");
    for (Entry<String, String> attribute : this.attributes.entrySet()) {
      sb.append(attribute.getKey() + "=" + attribute.getValue()).append(",");
    }

    return sb.delete(sb.length() - 1, sb.length()).append("]").toString();
  }
}
