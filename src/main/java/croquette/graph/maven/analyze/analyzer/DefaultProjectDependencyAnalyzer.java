package croquette.graph.maven.analyze.analyzer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ClassAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.ClassAnalysis;
import croquette.graph.maven.analyze.analysis.ClassIdentifier;
import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

@Component(role = ProjectDependencyAnalyzer.class)
public class DefaultProjectDependencyAnalyzer implements ProjectDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  /**
   * ClassAnalyzer
   */
  @Requirement(role = ClassAnalyzer.class, hint = "class")
  private ClassAnalyzer classAnalyzer;

  /**
   * DependencyAnalyzer
   */
  @Requirement
  private InternalClassDependencyAnalyzer dependencyAnalyzer;

  // ProjectDependencyAnalyzer methods --------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer#
   * analyze(org.apache.maven.project.MavenProject )
   */
  public ProjectDependencyAnalysis analyze(ArtifactFilter include, MavenProject project)
      throws ProjectDependencyAnalyzerException {
    if (include.include(project.getArtifact())) {
      try {
        Map<ArtifactIdentifier, Set<String>> artifactClassMap = buildArtifactClassMap(include, project);
        Map<String, InternalClassAnalysis> dependencyClasses = buildDependencyClasses(project);

        Map<String, ClassAnalysis> classAnalysisMap = buildProjectAnalysis(artifactClassMap, dependencyClasses);
        Set<ArtifactIdentifier> dependencies = artifactClassMap.keySet();
        return new ProjectDependencyAnalysis(project.getArtifact(), dependencies, classAnalysisMap);
      } catch (IOException exception) {
        throw new ProjectDependencyAnalyzerException("Cannot analyze dependencies", exception);
      }
    }
    return null;
  }

  private Map<String, ClassAnalysis> buildProjectAnalysis(Map<ArtifactIdentifier, Set<String>> artifactClassMap,
      Map<String, InternalClassAnalysis> dependencyClasses) {
    Map<String, ClassAnalysis> classAnalysisMap = new HashMap<String, ClassAnalysis>();

    for (InternalClassAnalysis internalClassAnalysis : dependencyClasses.values()) {
      ClassAnalysis classAnalysis = new ClassAnalysis(internalClassAnalysis.getArtifact(),
          internalClassAnalysis.getClassName());
      classAnalysisMap.put(classAnalysis.getClassName(), classAnalysis);
      for (String dependencyClassName : internalClassAnalysis.getDependencies()) {
        ArtifactIdentifier artifactIdentifier = null;
        if (dependencyClasses.containsKey(dependencyClassName)) {
          artifactIdentifier = internalClassAnalysis.getArtifact();
        } else {
          artifactIdentifier = findArtifactForClassName(artifactClassMap, dependencyClassName);
        }
        if (artifactIdentifier != null) {
          ClassIdentifier dependencyClassIdentifier = new ClassIdentifier(artifactIdentifier, dependencyClassName);
          classAnalysis.getDependencies().add(dependencyClassIdentifier);
        }
      }
    }
    return classAnalysisMap;
  }

  // private methods --------------------------------------------------------

  private Map<ArtifactIdentifier, Set<String>> buildArtifactClassMap(final ArtifactFilter filter, MavenProject project)
      throws IOException {
    Map<ArtifactIdentifier, Set<String>> artifactClassMap = new LinkedHashMap<ArtifactIdentifier, Set<String>>();

    @SuppressWarnings("unchecked")
    Set<Artifact> projectDependencyArtifacts = project.getArtifacts();

    Iterable<Artifact> projectDependencyArtifactsFiltered = Iterables.filter(project.getArtifacts(),
        new Predicate<Artifact>() {

          @Override
          public boolean apply(Artifact input) {
            return filter.include(input);
          }

        });

    Set<ArtifactIdentifier> dependencyArtifacts = Sets.newHashSet(Iterables.transform(
        projectDependencyArtifactsFiltered, new Function<Artifact, ArtifactIdentifier>() {

          @Override
          public ArtifactIdentifier apply(Artifact input) {
            return new ArtifactIdentifier(input);
          }
        }));

    for (ArtifactIdentifier artifact : dependencyArtifacts) {
      File file = artifact.getArtifact().getFile();

      if (file != null && file.getName().endsWith(".jar")) {
        // optimized solution for the jar case
        JarFile jarFile = new JarFile(file);

        try {
          Enumeration<JarEntry> jarEntries = jarFile.entries();

          Set<String> classes = new HashSet<String>();

          while (jarEntries.hasMoreElements()) {
            String entry = jarEntries.nextElement().getName();
            if (entry.endsWith(".class")) {
              String className = entry.replace('/', '.');
              className = className.substring(0, className.length() - ".class".length());
              classes.add(className);
            }
          }

          artifactClassMap.put(artifact, classes);
        } finally {
          try {
            jarFile.close();
          } catch (IOException ignore) {
            // ingore
          }
        }
      } else if (file != null && file.isDirectory()) {
        URL url = file.toURI().toURL();
        Set<String> classes = classAnalyzer.analyze(url);

        artifactClassMap.put(artifact, classes);
      }
    }

    return artifactClassMap;
  }

  protected Map<String, InternalClassAnalysis> buildDependencyClasses(MavenProject project) throws IOException {
    Map<String, InternalClassAnalysis> dependencyClasses = new HashMap<String, InternalClassAnalysis>();

    String outputDirectory = project.getBuild().getOutputDirectory();
    dependencyClasses.putAll(buildDependencyClasses(project.getArtifact(), outputDirectory));

    // String testOutputDirectory = project.getBuild().getTestOutputDirectory();
    // dependencyClasses.putAll(buildDependencyClasses(project.getArtifact(), testOutputDirectory));

    return dependencyClasses;
  }

  private Map<String, InternalClassAnalysis> buildDependencyClasses(Artifact artifact, String path) throws IOException {
    URL url = new File(path).toURI().toURL();
    Map<String, InternalClassAnalysis> analysisResult = dependencyAnalyzer.analyze(artifact, url);
    return analysisResult;
  }

  protected ArtifactIdentifier findArtifactForClassName(Map<ArtifactIdentifier, Set<String>> artifactClassMap,
      String className) {
    for (Map.Entry<ArtifactIdentifier, Set<String>> entry : artifactClassMap.entrySet()) {
      if (entry.getValue().contains(className)) {
        return entry.getKey();
      }
    }

    return null;
  }
}
