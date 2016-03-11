package croquette.graph.maven.analyze.analyzer.visitor.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;

import croquette.graph.maven.analyze.utils.ClassUtil;

public class ClassDependencyCollector extends Remapper {

  private final Set<String> classNames;

  public ClassDependencyCollector(final Set<String> classNames) {
    this.classNames = classNames;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String mapDesc(final String desc) {
    if (desc.startsWith("L")) {
      this.addType(desc.substring(1, desc.length() - 1));
    }
    return super.mapDesc(desc);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] mapTypes(final String[] types) {
    for (final String type : types) {
      this.addType(type);
    }
    return super.mapTypes(types);
  }

  private void addType(final String type) {
    final String className = ClassUtil.normalizeClassName(type);
    this.classNames.add(className);
  }

  @Override
  public String mapType(final String type) {
    this.addType(type);
    return type;
  }

  @Override
  public String map(String typeName) {
    if (typeName != null && typeName.contains("/")) {
      addType(typeName);
    }
    return super.map(typeName);
  }

  public static Set<String> getClassesUsedBy(final String name) throws IOException {
    final ClassReader reader = new ClassReader(name);
    final Set<String> classes = new TreeSet<String>(new Comparator<String>() {

      @Override
      public int compare(final String o1, final String o2) {
        return o1.compareTo(o2);
      }
    });
    final Remapper remapper = new ClassDependencyCollector(classes);
    final ClassVisitor inner = new EmptyVisitor();
    final RemappingClassAdapter visitor = new RemappingClassAdapter(inner, remapper);
    reader.accept(visitor, ClassReader.EXPAND_FRAMES);
    return classes;
  }

  public static Set<String> getClassesUsedBy(InputStream in) throws IOException {
    final ClassReader reader = new ClassReader(in);
    final Set<String> classes = new TreeSet<String>(new Comparator<String>() {

      @Override
      public int compare(final String o1, final String o2) {
        return o1.compareTo(o2);
      }
    });
    final Remapper remapper = new ClassDependencyCollector(classes);
    final ClassVisitor inner = new EmptyVisitor();
    final RemappingClassAdapter visitor = new RemappingClassAdapter(inner, remapper);
    reader.accept(visitor, ClassReader.EXPAND_FRAMES);
    return classes;
  }
}
