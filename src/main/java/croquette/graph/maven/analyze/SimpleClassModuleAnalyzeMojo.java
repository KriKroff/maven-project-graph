package croquette.graph.maven.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.JarEntryAnalysis;
import croquette.graph.maven.analyze.analysis.JarEntryDescription;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.utils.ClassUtil;

@Mojo(name = "simple-analyze-usage", aggregator = false, inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class SimpleClassModuleAnalyzeMojo extends AbstractAnalyzeMojo {

  /**
   * List of artifact to expand (show classes instead of module)
   *
   * @since 1.0.0
   */
  @Parameter(property = "expand", required = true)
  protected String expand;

  @Override
  protected void executeInternal() throws MojoExecutionException {
    List<String> expandPatterns = new ArrayList<String>();
    expandPatterns.add(this.expand);
    ArtifactFilter expandFilter = createIncludeArtifactFilter(expandPatterns);

    List<String> includePatterns = new ArrayList<String>();
    includePatterns.add(this.expand);
    includePatterns.add(this.project.getGroupId() + ":" + this.project.getArtifactId());
    ArtifactFilter includeFilter = createIncludeArtifactFilter(includePatterns);

    this.analyzer = "";

    ProjectDependencyAnalysis analysis = null;
    try {
      analysis = createProjectDependencyAnalyzer().analyze(includeFilter, this.project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    analyseClasses(analysis, expandFilter);
  }

  private void analyseClasses(ProjectDependencyAnalysis analysis, ArtifactFilter expandFilter) {

    Set<String> moduleClasses = new HashSet<String>();

    for (Entry<ArtifactIdentifier, Set<String>> entry : analysis.getArtifactsClassMap().entrySet()) {
      if (expandFilter.include(entry.getKey().getArtifact())) {
        moduleClasses.addAll(entry.getValue());
      }
    }

    Map<String, Set<ArtifactIdentifier>> usedClasses = new HashMap<String, Set<ArtifactIdentifier>>();

    for (JarEntryAnalysis classAnalysis : analysis.getDependencies().values()) {
      if (expandFilter.include(classAnalysis.getArtifact())) {
        moduleClasses.add(classAnalysis.getId());
      } else {
        for (JarEntryDescription dependency : classAnalysis.getDependencies()) {
          if (expandFilter.include(dependency.getArtifact())) {
            addUsedClass(usedClasses, classAnalysis, dependency);
          }
        }
      }
    }
    Set<String> unDirectlyUsedClasses = Sets.filter(Sets.difference(moduleClasses, usedClasses.keySet()),
        new Predicate<String>() {
          @Override
          public boolean apply(String input) {
            return input != null && input.indexOf('$') == -1;
          }
        });
    ;
    boolean found = false;

    Set<String> unused = new HashSet<String>(unDirectlyUsedClasses);

    Set<String> toAnalyseUsed = new HashSet<String>(usedClasses.keySet());
    List<Set<String>> transitiveUse = new ArrayList<Set<String>>();
    do {
      found = false;
      Set<String> toAnalyse = new HashSet<String>(toAnalyseUsed);
      toAnalyseUsed = new HashSet<String>(toAnalyse.size());
      for (String className : toAnalyse) {
        JarEntryAnalysis classAnalysis = analysis.getDependencies().get(className);
        if (classAnalysis != null) {
          for (JarEntryDescription dependency : classAnalysis.getDependencies()) {
            if (expandFilter.include(dependency.getArtifact())) {
              if (unused.remove(dependency.getId()) || unused.remove(ClassUtil.normalizeClassName(dependency.getId()))) {
                toAnalyseUsed.add(dependency.getId());
                toAnalyseUsed.add(ClassUtil.normalizeClassName(dependency.getId()));
                found = true;
              }
            }
          }
        }
        if (!toAnalyseUsed.isEmpty()) {
          transitiveUse.add(toAnalyseUsed);
        }
      }
    } while (found);

    toAnalyseUsed = null;

    List<String> sortedUnused = new ArrayList<String>(unused);
    Collections.sort(sortedUnused);

    getLog().info("Analysis Finished :");
    getLog().info("Unused Classes : " + sortedUnused.size());
    for (String className : sortedUnused) {
      getLog().info("- " + className);
    }

    getLog().info("Directly Used Classes : " + usedClasses.size());
    for (String className : usedClasses.keySet()) {
      getLog().info("- " + className);
    }
    getLog().info("Used Classes : " + (moduleClasses.size() - unused.size()));

  }

  private void addUsedClass(Map<String, Set<ArtifactIdentifier>> usedClasses, JarEntryDescription caller,
      JarEntryDescription dependency) {
    Set<ArtifactIdentifier> callers = usedClasses.get(dependency.getId());
    if (callers == null) {
      callers = new HashSet<ArtifactIdentifier>();
      usedClasses.put(dependency.getId(), callers);
    }
    callers.add(caller.getArtifactIdentifier());
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
