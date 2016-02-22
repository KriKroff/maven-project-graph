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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

/**
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: DefaultProjectDependencyAnalyzer.java 1635410 2014-10-30 07:03:49Z hboutemy $
 */
@Component(role = ProjectDependencyAnalyzer.class, hint = "aggregate")
public class AggregatingProjectDependencyAnalyzer implements ProjectDependencyAnalyzer {
  // fields -----------------------------------------------------------------

  private Log log = new SystemStreamLog();

  /**
   * DependencyAnalyzer
   */
  @Requirement
  private ProjectDependencyAnalyzer dependencyAnalyzer;

  // ProjectDependencyAnalyzer methods --------------------------------------

  /*
   * @see
   * org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer#analyze(org.apache.maven.project.MavenProject
   * )
   */
  public ProjectDependencyAnalysis analyze(MavenProject project) throws ProjectDependencyAnalyzerException {

    List<MavenProject> collectedProjects = project.getCollectedProjects();
    log.info("Analyzing " + project);
    if (collectedProjects.size() == 0) {
      collectedProjects = new ArrayList<MavenProject>();
      collectedProjects.add(project);
    } else {
      log.info(collectedProjects.size() + " to analyze");
    }
    Set<Artifact> usedDeclaredArtifacts = new LinkedHashSet<Artifact>();

    Set<Artifact> usedUndeclaredArtifacts = new LinkedHashSet<Artifact>();

    Set<Artifact> unusedDeclaredArtifacts = new LinkedHashSet<Artifact>();

    for (MavenProject collectedProject : collectedProjects) {
      System.out.println("Analyzing collected : " + collectedProject);
      ProjectDependencyAnalysis analysis = dependencyAnalyzer.analyze(collectedProject);
      usedDeclaredArtifacts.addAll(analysis.getClassDependencies());
      usedUndeclaredArtifacts.addAll(analysis.getUsedUndeclaredArtifacts());
      unusedDeclaredArtifacts.addAll(analysis.getUnusedDeclaredArtifacts());
    }

    return new ProjectDependencyAnalysis(usedDeclaredArtifacts, usedUndeclaredArtifacts, unusedDeclaredArtifacts);
  }

  /**
   * This method defines a new way to remove the artifacts by using the conflict id. We don't care about the version
   * here because there can be only 1 for a given artifact anyway.
   *
   * @param start initial set
   * @param remove set to exclude
   * @return set with remove excluded
   */
  private Set<Artifact> removeAll(Set<Artifact> start, Set<Artifact> remove) {
    Set<Artifact> results = new LinkedHashSet<Artifact>(start.size());

    for (Artifact artifact : start) {
      boolean found = false;

      for (Artifact artifact2 : remove) {
        if (artifact.getDependencyConflictId().equals(artifact2.getDependencyConflictId())) {
          found = true;
          break;
        }
      }

      if (!found) {
        results.add(artifact);
      }
    }

    return results;
  }

}
