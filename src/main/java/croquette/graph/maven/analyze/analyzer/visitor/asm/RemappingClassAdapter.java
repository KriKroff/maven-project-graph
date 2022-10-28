package croquette.graph.maven.analyze.analyzer.visitor.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

class RemappingClassAdapter extends ClassRemapper {

  public RemappingClassAdapter(ClassVisitor cv, Remapper remapper) {
    super(cv, remapper);
  }

  protected RemappingClassAdapter(final int api, final ClassVisitor cv, final Remapper remapper) {
    super(api, cv, remapper);
  }

  @Override
  protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
    return new RemappingMethodAdapter(methodVisitor, remapper);
  }

}
