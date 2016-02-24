package croquette.graph.maven.analyze;

import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.writer.GraphWriter;
import croquette.graph.maven.analyze.writer.dot.DotGraphWriter;

@Mojo(name = "analyze-aggregate", aggregator = true, inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class AggregatingAnalyzeMojo extends AbstractAnalyzeMojo {

  /**
   * List of artifact to analyze
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
  @Parameter(property = "expands", defaultValue = "")
  protected List<String> expands;

  /**
   * Output type (dot)
   * <p>
   * <ul>
   * <li>dot - 1.0.0</li>
   * </ul>
   * </p>
   *
   * @since 1.0.0
   */
  @Parameter(property = "outputType", defaultValue = "dot")
  protected String outputType;

  @Override
  protected void executeInternal() throws MojoExecutionException {
    ProjectDependencyAnalysis analysis = null;
    ArtifactFilter includeFilter = createIncludeArtifactFilter(includes);
    try {
      analysis = createProjectDependencyAnalyzer().analyze(includeFilter, this.project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }

    getLog().info("Writing graphContent");

    ArtifactFilter expandFilter = createIncludeArtifactFilter(expands);

    writeGraph(analysis, expandFilter);
  }

  protected void writeGraph(ProjectDependencyAnalysis analysis, ArtifactFilter expandFilter)
      throws MojoExecutionException {
    try {
      createGraphWriter(this.outputType).writeGraph(this.project, analysis, expandFilter);
    } catch (IOException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }
  }

  protected GraphWriter createGraphWriter(String outputType) {
    if ("gexf".equals(outputType)) {
      return null;
    }
    return new DotGraphWriter();
  }

}
