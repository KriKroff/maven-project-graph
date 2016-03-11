package croquette.graph.maven.analyze.analyzer.dependency.entry;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;

public interface IInternalEntryDependencyAnalyzer {

  Map<String, SimpleEntryAnalysis> analyze(Artifact artifact, URL url) throws IOException;
}