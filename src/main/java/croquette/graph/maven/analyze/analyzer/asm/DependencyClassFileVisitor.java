package croquette.graph.maven.analyze.analyzer.asm;

import java.util.Map;

import org.apache.maven.shared.dependency.analyzer.ClassFileVisitor;

import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;

public interface DependencyClassFileVisitor extends ClassFileVisitor {

  Map<String, InternalClassAnalysis> getDependencies();

}