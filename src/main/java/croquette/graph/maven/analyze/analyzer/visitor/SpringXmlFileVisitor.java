package croquette.graph.maven.analyze.analyzer.visitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;

public class SpringXmlFileVisitor implements IXmlFileVisitor {

  protected final ArtifactIdentifier artifactIdentifier;

  protected HashMap<String, Set<String>> collectors = new HashMap<String, Set<String>>();

  protected Map<String, SimpleEntryAnalysis> xmlDependencies = new HashMap<String, SimpleEntryAnalysis>();

  public SpringXmlFileVisitor(Artifact artifact) {
    this.artifactIdentifier = new ArtifactIdentifier(artifact);
  }

  @Override
  public void visitFile(String fileName, InputStream in) {
    Pattern dependencyClassPattern = Pattern.compile(".*class=\"([A-Za-z0-9.]+)\".*");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
    String line = null;
    Set<String> classNames = new HashSet<String>();
    collectors.put(artifactIdentifier.getArtifact().getArtifactId() + "_" + fileName, classNames);
    try {
      while ((line = bufferedReader.readLine()) != null) {
        Matcher matcher = dependencyClassPattern.matcher(line);
        if (matcher.matches()) {
          classNames.add(matcher.group(1));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, SimpleEntryAnalysis> getXmlDependencies() {
    return Maps.transformEntries(collectors, new EntryTransformer<String, Set, SimpleEntryAnalysis>() {
      @Override
      public SimpleEntryAnalysis transformEntry(String key, Set value) {
        return new SimpleEntryAnalysis(artifactIdentifier, key, value);
      }
    });
  }

}
