package croquette.graph.maven.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.JarEntryAnalysis;
import croquette.graph.maven.analyze.analysis.JarEntryDescription;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.utils.NoopArtifactFilter;

@Mojo(name = "analyze-usage", aggregator = false, inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class ClassModuleAnalyzeMojo extends AbstractAnalyzeMojo {

  @Override
  protected void executeInternal() throws MojoExecutionException {
    ArtifactFilter includeFilter = new NoopArtifactFilter();

    this.analyzer = "";

    ProjectDependencyAnalysis analysis = null;
    try {
      analysis = createProjectDependencyAnalyzer().analyze(includeFilter, this.project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    analyseClassesPerProjet(analysis);
  }

  private void analyseClassesPerProjet(ProjectDependencyAnalysis analysis) {
    ArtifactIdentifier currentProjectArtifact = new ArtifactIdentifier(this.project.getArtifact());

    Set<ArtifactIdentifier> declaredArtifacts = Sets.newHashSet(Iterables.transform(project.getDependencyArtifacts(),
        new Function<Artifact, ArtifactIdentifier>() {

          @Override
          public ArtifactIdentifier apply(Artifact input) {
            return new ArtifactIdentifier(input);
          }
        }));
    Set<ArtifactIdentifier> usedArtifacts = new HashSet<ArtifactIdentifier>();
    Set<ArtifactIdentifier> unusedArtifacts = new HashSet<ArtifactIdentifier>();

    for (Entry<ArtifactIdentifier, Set<String>> entry : analysis.getArtifactsClassMap().entrySet()) {
      if (!currentProjectArtifact.equals(entry.getKey())) {
        Set<String> moduleClasses = entry.getValue();
        ArtifactIdentifier currentArtifact = entry.getKey();

        Map<String, Set<ArtifactIdentifier>> directlyUsedClasses = new HashMap<String, Set<ArtifactIdentifier>>();

        for (JarEntryAnalysis entryAnalysis : analysis.getDependencies().values()) {
          for (JarEntryDescription dependency : entryAnalysis.getDependencies()) {
            if (currentArtifact.equals(dependency.getArtifactIdentifier())) {
              addUsedClass(directlyUsedClasses, entryAnalysis, dependency);
            }
          }
        }
        Set<String> unDirectlyUsedClasses = Sets.filter(Sets.difference(moduleClasses, directlyUsedClasses.keySet()),
            new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return input != null && input.indexOf('$') == -1;
              }
            });
        ;
        // boolean found = false;

        Set<String> unused = new HashSet<String>(unDirectlyUsedClasses);

        // Set<String> toAnalyseUsed = new HashSet<String>(directlyUsedClasses.keySet());
        // List<Set<String>> transitiveUse = new ArrayList<Set<String>>();
        // do {
        // found = false;
        // Set<String> toAnalyse = new HashSet<String>(toAnalyseUsed);
        // toAnalyseUsed = new HashSet<String>(toAnalyse.size());
        // for (String className : toAnalyse) {
        // JarEntryAnalysis classAnalysis = analysis.getDependencies().get(className);
        // if (classAnalysis != null) {
        // for (JarEntryDescription dependency : classAnalysis.getDependencies()) {
        // if (expandFilter.include(dependency.getArtifact())) {
        // if (unused.remove(dependency.getId())
        // || unused.remove(ClassUtil.normalizeClassName(dependency.getId()))) {
        // toAnalyseUsed.add(dependency.getId());
        // toAnalyseUsed.add(ClassUtil.normalizeClassName(dependency.getId()));
        // found = true;
        // }
        // }
        // }
        // }
        // if (!toAnalyseUsed.isEmpty()) {
        // transitiveUse.add(toAnalyseUsed);
        // }
        // }
        // } while (found);
        //
        // toAnalyseUsed = null;

        List<String> sortedUnused = new ArrayList<String>(unused);
        Collections.sort(sortedUnused);

        getLog().info("Analysis " + currentArtifact.toString());
        getLog().info("Classes : " + moduleClasses.size());
        getLog().info("Directly Used Classes : " + directlyUsedClasses.size());
        for (String className : directlyUsedClasses.keySet()) {
          getLog().debug("- " + className);
        }
        // getLog().info("Used Classes : " + (moduleClasses.size() - unused.size()));
        if (directlyUsedClasses.isEmpty()) {
          unusedArtifacts.add(currentArtifact);
        } else {
          usedArtifacts.add(currentArtifact);
        }
        getLog().info("------------");
      }
    }

    getLog().info("GLOBAL Analysis :");

    SetView<ArtifactIdentifier> declaredUnused = Sets.intersection(declaredArtifacts, unusedArtifacts);
    SetView<ArtifactIdentifier> undeclaredUsed = Sets.difference(usedArtifacts, declaredArtifacts);

    getLog().info("Used undeclared artifacts :");
    for (ArtifactIdentifier artifact : undeclaredUsed) {
      getLog().info("- " + artifact.getIdentifier());
    }
    getLog().info("");
    getLog().info("Unused declared artifacts :");
    for (ArtifactIdentifier artifact : declaredUnused) {
      getLog().info("- " + artifact.getIdentifier());
    }

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
