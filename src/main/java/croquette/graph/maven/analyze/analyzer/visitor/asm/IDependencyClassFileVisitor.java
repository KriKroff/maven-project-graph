package croquette.graph.maven.analyze.analyzer.visitor.asm;

import java.util.Map;

import org.apache.maven.shared.dependency.analyzer.ClassFileVisitor;

import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;

public interface IDependencyClassFileVisitor extends ClassFileVisitor {

  Map<String, SimpleEntryAnalysis> getDependencies();

}