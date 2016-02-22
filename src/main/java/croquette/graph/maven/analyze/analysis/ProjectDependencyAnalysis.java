package croquette.graph.maven.analyze.analysis;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;

import com.google.common.base.Joiner;

/**
 * Project dependencies analysis result.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: ProjectDependencyAnalysis.java 1635410 2014-10-30 07:03:49Z hboutemy $
 */
public class ProjectDependencyAnalysis {

  /**
   * Dependency map Class -> classes
   */
  private final Map<String, Set<String>> classDependencies;

  private final Set<Artifact> usedUndeclaredArtifacts;

  private final Set<Artifact> unusedDeclaredArtifacts;

  // constructors -----------------------------------------------------------

  public ProjectDependencyAnalysis() {
    this(null, null, null);
  }

  public ProjectDependencyAnalysis(Map<String, Set<String>> classDependencies, Set<Artifact> usedUndeclaredArtifacts,
      Set<Artifact> unusedDeclaredArtifacts) {
    this.classDependencies = classDependencies;
    this.usedUndeclaredArtifacts = safeCopy(usedUndeclaredArtifacts);
    this.unusedDeclaredArtifacts = safeCopy(unusedDeclaredArtifacts);
  }

  public Map<String, Set<String>> getClassDependencies() {
    return classDependencies;
  }

  /**
   * Used but not declared artifacts.
   */
  public Set<Artifact> getUsedUndeclaredArtifacts() {
    return usedUndeclaredArtifacts;
  }

  /**
   * Unused but declared artifacts.
   */
  public Set<Artifact> getUnusedDeclaredArtifacts() {
    return unusedDeclaredArtifacts;
  }

  /**
   * Filter not-compile scoped artifacts from unused declared.
   *
   * @return updated project dependency analysis
   * @since 1.3
   */
  public ProjectDependencyAnalysis ignoreNonCompile() {
    Set<Artifact> filteredUnusedDeclared = new HashSet<Artifact>(unusedDeclaredArtifacts);
    for (Iterator<Artifact> iter = filteredUnusedDeclared.iterator(); iter.hasNext();) {
      Artifact artifact = iter.next();
      if (!artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
        iter.remove();
      }
    }

    return new ProjectDependencyAnalysis(classDependencies, usedUndeclaredArtifacts, filteredUnusedDeclared);
  }

  /**
   * Force use status of some declared dependencies, to manually fix consequences of bytecode-level analysis which
   * happens to not detect some effective use (constants, annotation with source-retention, javadoc).
   *
   * @param forceUsedDependencies dependencies to move from "unused-declared" to "used-declared", with
   *          <code>groupId:artifactId</code> format
   * @return updated project dependency analysis
   * @throws ProjectDependencyAnalyzerException if dependencies forced were either not declared or already detected as
   *           used
   * @since 1.3
   */
  public ProjectDependencyAnalysis forceDeclaredDependenciesUsage(String[] forceUsedDependencies)
      throws ProjectDependencyAnalyzerException {
    Set<String> forced = new HashSet<String>(Arrays.asList(forceUsedDependencies));

    Set<Artifact> forcedUnusedDeclared = new HashSet<Artifact>(unusedDeclaredArtifacts);
    Set<Artifact> forcedUsedDeclared = new HashSet<Artifact>(usedDeclaredArtifacts);

    for (Iterator<Artifact> iter = forcedUnusedDeclared.iterator(); iter.hasNext();) {
      Artifact artifact = iter.next();

      if (forced.remove(artifact.getGroupId() + ':' + artifact.getArtifactId())) {
        // ok, change artifact status from unused-declared to used-declared
        iter.remove();
        forcedUsedDeclared.add(artifact);
      }
    }

    if (!forced.isEmpty()) {
      // trying to force dependencies as used-declared which were not declared or already detected as used
      Set<String> used = new HashSet<String>();
      for (Artifact artifact : usedDeclaredArtifacts) {
        String id = artifact.getGroupId() + ':' + artifact.getArtifactId();
        if (forced.remove(id)) {
          used.add(id);
        }
      }

      StringBuilder builder = new StringBuilder();
      if (!forced.isEmpty()) {
        builder.append("not declared: ").append(forced);
      }
      if (!used.isEmpty()) {
        if (builder.length() > 0) {
          builder.append(" and ");
        }
        builder.append("declared but already detected as used: ").append(used);
      }
      throw new ProjectDependencyAnalyzerException("Trying to force use of dependencies which are " + builder);
    }

    return new ProjectDependencyAnalysis(forcedUsedDeclared, usedUndeclaredArtifacts, forcedUnusedDeclared);
  }

  /*
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuilder buffer = new StringBuilder();

    if (!getClassDependencies().isEmpty()) {
      buffer.append("classDependencies=").append("\n");
      for (Entry<String, Set<String>> classDependency : getClassDependencies().entrySet()) {
        buffer.append(classDependency.getKey()).append(Joiner.on(';').join(classDependency.getValue())).append("\n");
      }

    }

    if (!getUsedUndeclaredArtifacts().isEmpty()) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }

      buffer.append("usedUndeclaredArtifacts=").append(getUsedUndeclaredArtifacts());
    }

    if (!getUnusedDeclaredArtifacts().isEmpty()) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }

      buffer.append("unusedDeclaredArtifacts=").append(getUnusedDeclaredArtifacts());
    }

    buffer.insert(0, "[");
    buffer.insert(0, getClass().getName());

    buffer.append("]");

    return buffer.toString();
  }

  // private methods --------------------------------------------------------

  private Set<Artifact> safeCopy(Set<Artifact> set) {
    return (set == null) ? Collections.<Artifact> emptySet() : Collections.unmodifiableSet(new LinkedHashSet<Artifact>(
        set));
  }

}
