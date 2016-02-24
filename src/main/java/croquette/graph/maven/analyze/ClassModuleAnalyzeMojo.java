package croquette.graph.maven.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import com.google.common.collect.Sets;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.ClassAnalysis;
import croquette.graph.maven.analyze.analysis.ClassIdentifier;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.writer.GraphWriter;
import croquette.graph.maven.analyze.writer.dot.DotGraphWriter;

@Mojo(name = "analyze-usage", aggregator = true, inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class ClassModuleAnalyzeMojo extends AbstractAnalyzeMojo {

  /**
   * List of artifact to include as Callers
   *
   * @since 1.0.0
   */
  @Parameter(property = "includes", defaultValue = "")
  protected List<String> includes;

  /**
   * List of artifact to expand (show classes instead of module)
   *
   * @since 1.0.0
   */
  @Parameter(property = "expand", required = true)
  protected String expand;

  @Parameter(property = "depthAnalysis", defaultValue = "true")
  protected boolean depthAnalysis;

  protected GraphWriter createGraphWriter(String outputType) {
    if ("gexf".equals(outputType)) {
      return null;
    }
    return new DotGraphWriter();
  }

  @Override
  protected void executeInternal() throws MojoExecutionException {
    List<String> expandPatterns = new ArrayList<String>();
    expandPatterns.add(this.expand);
    ArtifactFilter expandFilter = createIncludeArtifactFilter(expandPatterns);

    List<String> includePatterns = new ArrayList<String>();
    includePatterns.add(this.expand);
    includePatterns.addAll(this.includes);
    ArtifactFilter includeFilter = createIncludeArtifactFilter(includePatterns);

    ProjectDependencyAnalysis analysis = null;
    try {
      analysis = createProjectDependencyAnalyzer().analyze(includeFilter, this.project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    getLog().info("Writing graphContent");

    analyseClasses(analysis, expandFilter);
  }

  private void analyseClasses(ProjectDependencyAnalysis analysis, ArtifactFilter expandFilter) {

    Set<String> moduleClasses = new HashSet<String>();

    Map<String, Set<ArtifactIdentifier>> usedClasses = new HashMap<String, Set<ArtifactIdentifier>>();

    for (ClassAnalysis classAnalysis : analysis.getClassDependencies().values()) {
      if (expandFilter.include(classAnalysis.getArtifact())) {
        moduleClasses.add(classAnalysis.getClassName());
      } else {
        for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
          if (expandFilter.include(dependency.getArtifact())) {
            addUsedClass(usedClasses, classAnalysis, dependency);
          }
        }
      }
    }
    Set<String> unDirectlyUsedClasses = Sets.difference(moduleClasses, usedClasses.keySet());
    boolean found = false;

    Set<String> unUsed = new HashSet<String>(unDirectlyUsedClasses);

    Set<String> toAnalyseUsed = new HashSet<String>(usedClasses.keySet());
    List<Set<String>> transitiveUse = new ArrayList<Set<String>>();
    do {
      found = false;
      Set<String> toAnalyse = new HashSet<String>(toAnalyseUsed);
      toAnalyseUsed = new HashSet<String>(toAnalyse.size());
      for (String className : toAnalyse) {
        ClassAnalysis classAnalysis = analysis.getClassDependencies().get(className);
        if (classAnalysis != null) {
          for (ClassIdentifier dependency : classAnalysis.getDependencies()) {
            if (expandFilter.include(dependency.getArtifact())) {
              if (unUsed.remove(dependency.getClassName())
                  || unUsed.remove(removeStaticClass(dependency.getClassName()))) {
                toAnalyseUsed.add(dependency.getClassName());
                toAnalyseUsed.add(removeStaticClass(dependency.getClassName()));
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

    getLog().info("Analysis Finished :");
    getLog().info("Unused Classes : " + unUsed.size());
    for (String className : unUsed) {
      getLog().info("- " + className);
    }

    getLog().info("Directly Used Classes : " + usedClasses.size());
    getLog().info("Used Classes : " + (moduleClasses.size() - unUsed.size()));

  }

  private String removeStaticClass(String className) {
    return className.replaceAll("\\$.*", "");
  }

  private void addUsedClass(Map<String, Set<ArtifactIdentifier>> usedClasses, ClassIdentifier caller,
      ClassIdentifier dependency) {
    Set<ArtifactIdentifier> callers = usedClasses.get(dependency.getClassName());
    if (callers == null) {
      callers = new HashSet<ArtifactIdentifier>();
      usedClasses.put(dependency.getClassName(), callers);
    }
    callers.add(caller.getArtifactIdentifier());
  }
}
