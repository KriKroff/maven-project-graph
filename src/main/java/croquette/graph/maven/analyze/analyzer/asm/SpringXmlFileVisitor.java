package croquette.graph.maven.analyze.analyzer.asm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import croquette.graph.maven.analyze.analyzer.XmlFileVisitor;

public class SpringXmlFileVisitor implements XmlFileVisitor {
  protected HashMap<String, Set<String>> collectors = new HashMap<String, Set<String>>();

  protected Set<String> xmlDependencies = new HashSet<String>();

  @Override
  public void visitFile(String fileName, InputStream in) {
    Pattern dependencyClassPattern = Pattern.compile(".*class=\"([A-Za-z0-9.]+)\".*");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
    String line = null;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        Matcher matcher = dependencyClassPattern.matcher(line);
        if (matcher.matches()) {
          xmlDependencies.add(matcher.group(1));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Set<String> getXmlDependencies() {
    return xmlDependencies;
  }

}
