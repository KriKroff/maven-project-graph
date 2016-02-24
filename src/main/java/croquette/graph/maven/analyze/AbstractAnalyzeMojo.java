package croquette.graph.maven.analyze;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.StrictPatternIncludesArtifactFilter;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;
import croquette.graph.maven.analyze.analyzer.ProjectDependencyAnalyzer;

public abstract class AbstractAnalyzeMojo extends AbstractMojo implements Contextualizable {
  // fields -----------------------------------------------------------------

  /**
   * The plexus context to look-up the right {@link ProjectDependencyAnalyzer} implementation depending on the mojo
   * configuration.
   */
  private Context context;

  /**
   * The Maven project to analyze.
   */
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  /**
   * Specify the project dependency analyzer to use (plexus component role-hint). By default, <a
   * href="/shared/maven-dependency-analyzer/">maven-dependency-analyzer</a> is used.
   *
   * To use this, you must declare a dependency for this plugin that contains the code for the analyzer. The analyzer
   * must have a declared Plexus role name, and you specify the role name here.
   *
   * @since 2.2
   */
  @Parameter(property = "analyzer", defaultValue = "aggregate")
  protected String analyzer;

  /**
   * Output used dependencies.
   */
  @Parameter(property = "verbose", defaultValue = "false")
  protected boolean verbose;

  /**
   * Output the xml for the missing dependencies (used but not declared).
   *
   * @since 2.0-alpha-5
   */
  @Parameter(property = "outputXML", defaultValue = "false")
  protected boolean outputXML;

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

    // if ("pom".equals(project.getPackaging())) {
    // getLog().info("Skipping pom project");
    // return;
    // }

    // if (outputDirectory == null || !outputDirectory.exists()) {
    // getLog().info("Skipping project with no build directory");
    // return;
    // }
  }

  protected abstract void executeInternal() throws MojoExecutionException;

  protected ProjectDependencyAnalysis executeProjectAnalysis(ArtifactFilter includeFilter)
      throws MojoExecutionException {
    try {
      return createProjectDependencyAnalyzer().analyze(includeFilter, this.project);
    } catch (ProjectDependencyAnalyzerException exception) {
      throw new MojoExecutionException("Cannot analyze dependencies", exception);
    }
  }

  protected ProjectDependencyAnalyzer createProjectDependencyAnalyzer() throws MojoExecutionException {

    final String role = ProjectDependencyAnalyzer.ROLE;
    final String roleHint = analyzer;

    try {
      final PlexusContainer container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);

      return (ProjectDependencyAnalyzer) container.lookup(role, roleHint);
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to instantiate ProjectDependencyAnalyser with role " + role
          + " / role-hint " + roleHint, exception);
    }
  }

  @Override
  public void contextualize(Context context) throws ContextException {
    this.context = context;
  }

  protected final boolean isSkip() {
    return skip;
  }

  // private methods --------------------------------------------------------

  private void logArtifacts(Set<Artifact> artifacts, boolean warn) {
    if (artifacts.isEmpty()) {
      getLog().info("   None");
    } else {
      for (Artifact artifact : artifacts) {
        // called because artifact will set the version to -SNAPSHOT only if I do this. MNG-2961
        artifact.isSnapshot();

        if (warn) {
          getLog().warn("   " + artifact);
        } else {
          getLog().info("   " + artifact);
        }

      }
    }
  }

  private void writeDependencyXML(Set<Artifact> artifacts) {
    if (!artifacts.isEmpty()) {
      getLog().info("Add the following to your pom to correct the missing dependencies: ");

      StringWriter out = new StringWriter();
      PrettyPrintXMLWriter writer = new PrettyPrintXMLWriter(out);

      for (Artifact artifact : artifacts) {
        // called because artifact will set the version to -SNAPSHOT only if I do this. MNG-2961
        artifact.isSnapshot();

        writer.startElement("dependency");
        writer.startElement("groupId");
        writer.writeText(artifact.getGroupId());
        writer.endElement();
        writer.startElement("artifactId");
        writer.writeText(artifact.getArtifactId());
        writer.endElement();
        writer.startElement("version");
        writer.writeText(artifact.getBaseVersion());
        if (!StringUtils.isBlank(artifact.getClassifier())) {
          writer.startElement("classifier");
          writer.writeText(artifact.getClassifier());
          writer.endElement();
        }
        writer.endElement();

        if (!Artifact.SCOPE_COMPILE.equals(artifact.getScope())) {
          writer.startElement("scope");
          writer.writeText(artifact.getScope());
          writer.endElement();
        }
        writer.endElement();
      }

      getLog().info("\n" + out.getBuffer());
    }
  }

  private List<Artifact> filterDependencies(Set<Artifact> artifacts, String[] excludes) throws MojoExecutionException {
    ArtifactFilter filter = new StrictPatternIncludesArtifactFilter(Arrays.asList(excludes));
    List<Artifact> result = new ArrayList<Artifact>();

    for (Iterator<Artifact> it = artifacts.iterator(); it.hasNext();) {
      Artifact artifact = it.next();
      if (!filter.include(artifact)) {
        it.remove();
        result.add(artifact);
      }
    }

    return result;
  }

  protected ArtifactFilter createIncludeArtifactFilter(List<String> includes) {
    if (includes != null && !includes.isEmpty()) {
      return new StrictPatternIncludesArtifactFilter(includes);
    }
    return new NoopArtifactFilter();
  }

}
