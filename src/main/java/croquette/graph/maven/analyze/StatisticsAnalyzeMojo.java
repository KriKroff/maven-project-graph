package croquette.graph.maven.analyze;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ClassAnalyzer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;

@Mojo(name = "stats", aggregator = false, inheritByDefault = false, threadSafe = true)
public class StatisticsAnalyzeMojo extends AbstractMojo {

  /**
   * ClassAnalyzer
   */
  private ClassAnalyzer classAnalyzer;

  protected ClassAnalyzer createClassAnalyzer() throws MojoExecutionException {
    try {
      final PlexusContainer container = (PlexusContainer) this.context.get(PlexusConstants.PLEXUS_KEY);

      return (ClassAnalyzer) container.lookup(ClassAnalyzer.ROLE, "class");
    } catch (Exception exception) {
      throw new MojoExecutionException("Failed to instantiate ProjectDependencyAnalyser with role "
          + ClassAnalyzer.ROLE + " / role-hint class", exception);
    }
  }

  @Override
  protected void executeInternal() throws MojoExecutionException {
    PackageAnalysis packageAnalysis = null;
    classAnalyzer = createClassAnalyzer();
    try {
      packageAnalysis = getPackages(project);
    } catch (IOException e) {
      throw new MojoExecutionException("Exception during packages crawl", e);
    }

    info("Number of packages : " + packageAnalysis.getPackages().size());
    info("Number of classes : " + packageAnalysis.getClasses().size());
    if (packageAnalysis.getClasses().size() > 0) {
      info("Number of classes per package (Avg)  : "
          + Math.round(packageAnalysis.getClasses().size() / packageAnalysis.getPackages().size()));
      if (packageAnalysis.getPackages().size() > 0) {
        String biggestPackage = null;
        int nbClasses = -1;
        for (Entry<String, Set<String>> entry : packageAnalysis.getPackages().entrySet()) {
          if (entry.getValue().size() > nbClasses) {
            nbClasses = entry.getValue().size();
            biggestPackage = entry.getKey();
          }
        }
        info("Biggest package (" + nbClasses + " classes) : " + biggestPackage);
      }
    }
  }

  private PackageAnalysis getPackages(MavenProject project) throws IOException {
    Map<String, Set<String>> packages = new HashMap<String, Set<String>>();
    Set<String> classes = new HashSet<String>();
    File file = project.getArtifact().getFile();
    if (file == null) {
      file = new File(project.getBuild().getOutputDirectory());
    }
    if (file != null && file.getName().endsWith(".jar")) {
      // optimized solution for the jar case
      JarFile jarFile = new JarFile(file);

      try {
        Enumeration<JarEntry> jarEntries = jarFile.entries();

        while (jarEntries.hasMoreElements()) {
          String entry = jarEntries.nextElement().getName();
          if (entry.endsWith(".class")) {
            String className = entry.replace('/', '.').replaceAll("\\$.*", "");
            className = className.substring(0, className.length() - ".class".length());
            classes.add(className);

            String packageName = className.substring(0, className.lastIndexOf("."));
            addElement(packages, packageName, className);
          }
        }
      } finally {
        try {
          jarFile.close();
        } catch (IOException ignore) {
          // ingore
        }
      }
    } else if (file != null && file.isDirectory()) {
      System.out.println("ByUrl");
      URL url = file.toURI().toURL();
      Set<String> analyzedClasses = classAnalyzer.analyze(url);
      for (String className : analyzedClasses) {
        className = className.replaceAll("\\$.*", "");
        classes.add(className);
        String packageName = className.substring(0, className.lastIndexOf("."));
        addElement(packages, packageName, className);
      }
    } else {
      System.out.println("None" + file);

    }
    return new PackageAnalysis(packages, classes);
  }

  protected void info(String message) {
    getLog().info("[STATS]" + message);
  }

  private void addElement(Map<String, Set<String>> packages, String packageName, String className) {
    Set<String> classes = packages.get(packageName);
    if (classes == null) {
      classes = new HashSet<String>();
      packages.put(packageName, classes);
    }
    classes.add(className);
  }

  protected static class PackageAnalysis {
    private final Map<String, Set<String>> packages;
    private final Set<String> classes;

    public PackageAnalysis(Map<String, Set<String>> packages, Set<String> classes) {
      this.packages = packages;
      this.classes = classes;
    }

    public Map<String, Set<String>> getPackages() {
      return packages;
    }

    public Set<String> getClasses() {
      return classes;
    }
  }

}
