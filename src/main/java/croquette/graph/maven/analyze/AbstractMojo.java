package croquette.graph.maven.analyze;

import java.util.List;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.StrictPatternIncludesArtifactFilter;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import croquette.graph.maven.analyze.analyzer.dependency.IProjectDependencyAnalyzer;
import croquette.graph.maven.analyze.utils.NoopArtifactFilter;

public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo implements Contextualizable {
  // fields -----------------------------------------------------------------

  /**
   * The plexus context to look-up the right {@link IProjectDependencyAnalyzer} implementation depending on the mojo
   * configuration.
   */
  protected Context context;

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  /**
   * Skip plugin execution completely.
   *
   * @since 2.7
   */
  @Parameter(property = "croquette.maven.skip", defaultValue = "false")
  protected boolean skip;

  // Mojo methods -----------------------------------------------------------

  /*
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      getLog().info("Skipping plugin execution");
      return;
    }

    executeInternal();
  }

  protected abstract void executeInternal() throws MojoExecutionException;

  @Override
  public void contextualize(Context context) throws ContextException {
    this.context = context;
  }

  protected final boolean isSkip() {
    return skip;
  }

  protected ArtifactFilter createIncludeArtifactFilter(List<String> includes) {
    if (includes != null && !includes.isEmpty()) {
      return new StrictPatternIncludesArtifactFilter(includes);
    }
    return new NoopArtifactFilter();
  }

}
