package croquette.graph.maven.analyze.analyzer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.ClassFileVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultAnnotationVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultClassVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultFieldVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultMethodVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.DefaultSignatureVisitor;
import org.apache.maven.shared.dependency.analyzer.asm.ResultCollector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureVisitor;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import croquette.graph.maven.analyze.ArtifactUtils;
import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;

/**
 * Computes the set of classes referenced by visited class files, using <a
 * href="DependencyVisitor.html">DependencyVisitor</a>.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: DependencyClassFileVisitor.java 1589585 2014-04-24 04:58:35Z olamy $
 * @see #getDependencies()
 */
public class DependencyClassFileVisitor implements ClassFileVisitor {
  // fields -----------------------------------------------------------------

  protected HashMap<String, ResultCollector> collectors = new HashMap<String, ResultCollector>();

  protected String artifactIdentifier;

  // constructors -----------------------------------------------------------

  public DependencyClassFileVisitor(Artifact artifact) {
    artifactIdentifier = ArtifactUtils.versionLess(artifact);
  }

  // ClassFileVisitor methods -----------------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.ClassFileVisitor#visitClass(java.lang.String, java.io.InputStream)
   */
  public void visitClass(String className, InputStream in) {
    try {
      ClassReader reader = new ClassReader(in);

      ResultCollector resultCollector = new ResultCollector();
      collectors.put(className, resultCollector);

      AnnotationVisitor annotationVisitor = new DefaultAnnotationVisitor(resultCollector);
      SignatureVisitor signatureVisitor = new DefaultSignatureVisitor(resultCollector);
      FieldVisitor fieldVisitor = new DefaultFieldVisitor(annotationVisitor, resultCollector);
      MethodVisitor mv = new DefaultMethodVisitor(annotationVisitor, signatureVisitor, resultCollector);
      ClassVisitor classVisitor = new DefaultClassVisitor(signatureVisitor, annotationVisitor, fieldVisitor, mv,
          resultCollector);

      reader.accept(classVisitor, 0);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (IndexOutOfBoundsException e) {
      // some bug inside ASM causes an IOB exception. Log it and move on?
      // this happens when the class isn't valid.
      System.out.println("Unable to process: " + className);
    }
  }

  // public methods ---------------------------------------------------------

  /**
   * @return the set of classes referenced by visited class files
   */
  public List<InternalClassAnalysis> getDependencies() {
    return new ArrayList<InternalClassAnalysis>(Maps.transformEntries(collectors,
        new EntryTransformer<String, ResultCollector, InternalClassAnalysis>() {
          @Override
          public InternalClassAnalysis transformEntry(String key, ResultCollector value) {
            return new InternalClassAnalysis(artifactIdentifier, key, value.getDependencies());
          }
        }).values());
  }
}
