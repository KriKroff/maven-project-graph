package croquette.graph.maven.analyze.analyzer.visitor.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Remapper;

class RemappingClassAdapter extends org.objectweb.asm.commons.RemappingClassAdapter {

  public RemappingClassAdapter(ClassVisitor cv, Remapper remapper) {
    super(cv, remapper);
  }

  protected RemappingClassAdapter(final int api, final ClassVisitor cv, final Remapper remapper) {
    super(api, cv, remapper);
  }

  @Override
  protected MethodVisitor createRemappingMethodAdapter(int access, String newDesc, MethodVisitor mv) {
    return new RemappingMethodAdapter(access, newDesc, mv, remapper);
  }

}
