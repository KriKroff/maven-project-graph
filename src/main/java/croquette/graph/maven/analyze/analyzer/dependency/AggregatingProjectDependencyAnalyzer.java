package croquette.graph.maven.analyze.analyzer.dependency;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

@Component(role = IProjectDependencyAnalyzer.class, hint = "aggregate")
public class AggregatingProjectDependencyAnalyzer implements IProjectDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  private Log log = new SystemStreamLog();

  @Requirement
  private IProjectDependencyAnalyzer dependencyAnalyzer;

  public ProjectDependencyAnalysis analyze(final ArtifactFilter includeFilter, MavenProject project)
      throws ProjectDependencyAnalyzerException {

    List<MavenProject> collectedProjects = project.getCollectedProjects();
    log.info("Analyzing " + project);
    if (collectedProjects.size() == 0) {
      collectedProjects = new ArrayList<MavenProject>();
      collectedProjects.add(project);
    } else {
      log.info(collectedProjects.size() + " to analyze");
    }

    List<MavenProject> filteredProjects = Lists.newArrayList(Iterables.filter(collectedProjects,
        new Predicate<MavenProject>() {
          @Override
          public boolean apply(MavenProject input) {
            return includeFilter.include(input.getArtifact());
          }
        }));

    List<ProjectDependencyAnalysis> analyses = new ArrayList<ProjectDependencyAnalysis>();

    for (MavenProject collectedProject : filteredProjects) {
      analyses.add(dependencyAnalyzer.analyze(includeFilter, collectedProject));
    }

    return mergeProjectDependencyAnalysis(project, analyses);
  }

  private ProjectDependencyAnalysis mergeProjectDependencyAnalysis(MavenProject project,
      List<ProjectDependencyAnalysis> analyses) {
    ProjectDependencyAnalysis mergedAnalysis = new ProjectDependencyAnalysis(project.getArtifact());
    for (ProjectDependencyAnalysis analysis : analyses) {
      mergedAnalysis.getArtifactsClassMap().putAll(analysis.getArtifactsClassMap());
      mergedAnalysis.getDependencies().putAll(analysis.getDependencies());
    }
    return mergedAnalysis;
  }

}
