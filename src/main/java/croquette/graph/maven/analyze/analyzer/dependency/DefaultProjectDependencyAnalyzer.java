package croquette.graph.maven.analyze.analyzer.dependency;

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
import croquette.graph.maven.analyze.analysis.JarEntryAnalysis;
import croquette.graph.maven.analyze.analysis.JarEntryDescription;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.analysis.SimpleEntryAnalysis;
import croquette.graph.maven.analyze.analyzer.dependency.entry.IInternalEntryDependencyAnalyzer;
import croquette.graph.maven.analyze.utils.ClassUtil;

@Component(role = IProjectDependencyAnalyzer.class)
public class DefaultProjectDependencyAnalyzer implements IProjectDependencyAnalyzer {

  /*
   * JAR Analyzer collecting all classes for a buildpath/jar
   */
  @Requirement(role = ClassAnalyzer.class)
  private ClassAnalyzer jarAnalyzer;

  /**
   * Class Analyzer collecting dependencies for a single class
   */
  @Requirement(role = IInternalEntryDependencyAnalyzer.class, hint = "class")
  private IInternalEntryDependencyAnalyzer classDependencyAnalyzer;

  /**
   * XmlAnalyzer collecting dependencies for a single xmlFile
   */
  @Requirement(role = IInternalEntryDependencyAnalyzer.class, hint = "xml")
  private IInternalEntryDependencyAnalyzer xmlDependencyAnalyzer;

  public ProjectDependencyAnalysis analyze(ArtifactFilter include, MavenProject project)
      throws ProjectDependencyAnalyzerException {
    if (include.include(project.getArtifact())) {
      try {
        Map<ArtifactIdentifier, Set<String>> artifactClassMap = buildArtifactClassMap(include, project);
        Map<String, SimpleEntryAnalysis> dependencies = new HashMap<String, SimpleEntryAnalysis>();
        dependencies.putAll(buildClassesDependencies(project));
        dependencies.putAll(buildDependencyXml(project));

        Map<String, JarEntryAnalysis> classAnalysisMap = buildProjectAnalysis(artifactClassMap, dependencies);
        return new ProjectDependencyAnalysis(project.getArtifact(), artifactClassMap, classAnalysisMap);
      } catch (IOException exception) {
        throw new ProjectDependencyAnalyzerException("Cannot analyze dependencies", exception);
      }
    }
    return null;
  }

  private Map<String, JarEntryAnalysis> buildProjectAnalysis(Map<ArtifactIdentifier, Set<String>> artifactClassMap,
      Map<String, SimpleEntryAnalysis> dependencyClasses) {
    Map<String, JarEntryAnalysis> entryAnalysisMap = new HashMap<String, JarEntryAnalysis>();

    for (SimpleEntryAnalysis internalClassAnalysis : dependencyClasses.values()) {
      JarEntryAnalysis entryAnalysis = new JarEntryAnalysis(internalClassAnalysis.getArtifactIdentifier(),
          internalClassAnalysis.getId());
      entryAnalysisMap.put(entryAnalysis.getId(), entryAnalysis);
      for (String dependencyClassName : internalClassAnalysis.getDependencies()) {
        ArtifactIdentifier artifactIdentifier = null;
        if (dependencyClasses.containsKey(dependencyClassName)) {
          artifactIdentifier = internalClassAnalysis.getArtifactIdentifier();
        } else {
          artifactIdentifier = ClassUtil.findArtifactForClassName(artifactClassMap, dependencyClassName);
        }
        if (artifactIdentifier != null) {
          JarEntryDescription dependencyClassIdentifier = new JarEntryDescription(artifactIdentifier,
              dependencyClassName);
          entryAnalysis.getDependencies().add(dependencyClassIdentifier);
        }
      }
    }
    return entryAnalysisMap;
  }

  private Map<ArtifactIdentifier, Set<String>> buildArtifactClassMap(final ArtifactFilter filter, MavenProject project)
      throws IOException {
    Map<ArtifactIdentifier, Set<String>> artifactClassMap = new LinkedHashMap<ArtifactIdentifier, Set<String>>();

    @SuppressWarnings("unchecked")
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
        Set<String> classes = jarAnalyzer.analyze(url);

        artifactClassMap.put(artifact, classes);
      }
    }

    return artifactClassMap;
  }

  protected Map<String, SimpleEntryAnalysis> buildClassesDependencies(MavenProject project) throws IOException {
    String outputDirectory = project.getBuild().getOutputDirectory();
    URL url = new File(outputDirectory).toURI().toURL();
    return classDependencyAnalyzer.analyze(project.getArtifact(), url);
  }

  private Map<? extends String, ? extends SimpleEntryAnalysis> buildDependencyXml(MavenProject project)
      throws IOException {

    String outputDirectory = project.getBuild().getOutputDirectory();
    URL url = new File(outputDirectory).toURI().toURL();

    return xmlDependencyAnalyzer.analyze(project.getArtifact(), url);

  }
}
