package croquette.graph.maven.analyze.analyzer.asm;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import croquette.graph.maven.analyze.analysis.ArtifactIdentifier;
import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;

public class RemapperDependencyClassFileVisitor implements DependencyClassFileVisitor {

  protected HashMap<String, Set<String>> collectors = new HashMap<String, Set<String>>();

  private ArtifactIdentifier artifactIdentifier;

  public RemapperDependencyClassFileVisitor(Artifact artifact) {
    this.artifactIdentifier = new ArtifactIdentifier(artifact);
  }

  public RemapperDependencyClassFileVisitor(ArtifactIdentifier artifactIdentifier) {
    this.artifactIdentifier = artifactIdentifier;
  }

  // ClassFileVisitor methods -----------------------------------------------

  /*
   * @see org.apache.maven.shared.dependency.analyzer.ClassFileVisitor#visitClass(java.lang.String, java.io.InputStream)
   */
  public void visitClass(String className, InputStream in) {

    try {
      Set<String> classNames = null;
      try {
        classNames = Collector.getClassesUsedBy(className);
      } catch (IOException e) {
        classNames = Collector.getClassesUsedBy(in);
      }

      /*
       * Sets.newHashSet(Iterables.transform(classesUsedBy, new Function<Class, String>() {
       * 
       * @Override public String apply(Class input) { return input.getName(); } }));
       */
      collectors.put(className, classNames);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (IndexOutOfBoundsException e) {
      // some bug inside ASM causes an IOB exception. Log it and move on?
      // this happens when the class isn't valid.
      System.out.println("Unable to process: " + className);
    }
  }

  // public methods ---------------------------------------------------------

  /*
   * (non-Javadoc)
   * 
   * @see croquette.graph.maven.analyze.analyzer.asm.DependencyClassFileVisitor#getDependencies()
   */
  @Override
  public Map<String, InternalClassAnalysis> getDependencies() {
    return Maps.transformEntries(collectors, new EntryTransformer<String, Set, InternalClassAnalysis>() {
      @Override
      public InternalClassAnalysis transformEntry(String key, Set value) {
        return new InternalClassAnalysis(artifactIdentifier, key, value);
      }
    });
  }
}
