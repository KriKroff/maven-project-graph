package croquette.graph.maven.analyze.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.codehaus.plexus.util.DirectoryScanner;

import croquette.graph.maven.analyze.analyzer.visitor.IXmlFileVisitor;

public class XmlFileVisitorUtils {

  private static final String[] XML_INCLUDES = {"**/*.xml"};

  private XmlFileVisitorUtils() {
  }

  public static void accept(URL url, IXmlFileVisitor visitor) throws IOException {
    if (url.getPath().endsWith(".jar")) {
      acceptJar(url, visitor);
    } else if (url.getProtocol().equalsIgnoreCase("file")) {
      try {
        File file = new File(new URI(url.toString()));

        if (file.isDirectory()) {
          acceptDirectory(file, visitor);
        } else if (file.exists()) {
          throw new IllegalArgumentException("Cannot accept visitor on URL: " + url);
        }
      } catch (URISyntaxException exception) {
        IllegalArgumentException e = new IllegalArgumentException("Cannot accept visitor on URL: " + url);
        e.initCause(exception);
        throw e;
      }
    } else {
      throw new IllegalArgumentException("Cannot accept visitor on URL: " + url);
    }
  }

  // private methods --------------------------------------------------------

  private static void acceptJar(URL url, IXmlFileVisitor visitor) throws IOException {
    JarInputStream in = new JarInputStream(url.openStream());
    try {
      JarEntry entry = null;

      while ((entry = in.getNextJarEntry()) != null) {
        String name = entry.getName();
        if (name.endsWith(".xml")) {
          visitXML(name, in, visitor);
        }
      }
    } finally {
      in.close();
    }
  }

  private static void acceptDirectory(File directory, IXmlFileVisitor visitor) throws IOException {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("File is not a directory");
    }

    DirectoryScanner scanner = new DirectoryScanner();

    scanner.setBasedir(directory);
    scanner.setIncludes(XML_INCLUDES);

    scanner.scan();

    String[] paths = scanner.getIncludedFiles();

    for (String path : paths) {
      path = path.replace(File.separatorChar, '/');

      File file = new File(directory, path);
      FileInputStream in = new FileInputStream(file);

      try {
        visitXML(path, in, visitor);
      } finally {
        in.close();
      }
    }
  }

  private static void visitXML(String path, InputStream in, IXmlFileVisitor visitor) {
    if (!path.endsWith(".xml")) {
      throw new IllegalArgumentException("Path is not a xmlFile");
    }
    visitor.visitFile(path, in);
  }
}
