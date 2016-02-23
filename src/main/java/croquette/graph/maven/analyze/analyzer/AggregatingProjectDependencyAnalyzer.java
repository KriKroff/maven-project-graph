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
import java.util.List;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
  public ProjectDependencyAnalysis analyze(final ArtifactFilter includeFilter, MavenProject project)
      throws ProjectDependencyAnalyzerException {

    List<MavenProject> collectedProjects = project.getCollectedProjects();
    log.info("Analyzing " + project);
    if (collectedProjects.size() == 0) {
      collectedProjects = new ArrayList<MavenProject>();
      collectedProjects.add(project);
    } else {
      log.info(collectedProjects.size() + " to analyze");
    }

    List<MavenProject> filteredProjects = Lists.newArrayList(Iterables.filter(collectedProjects,
        new Predicate<MavenProject>() {
          @Override
          public boolean apply(MavenProject input) {
            return includeFilter.include(input.getArtifact());
          }
        }));

    List<ProjectDependencyAnalysis> analyses = new ArrayList<ProjectDependencyAnalysis>();

    for (MavenProject collectedProject : filteredProjects) {
      analyses.add(dependencyAnalyzer.analyze(includeFilter, collectedProject));
    }

    return mergeProjectDependencyAnalysis(project, analyses);
  }

  private ProjectDependencyAnalysis mergeProjectDependencyAnalysis(MavenProject project,
      List<ProjectDependencyAnalysis> analyses) {
    ProjectDependencyAnalysis mergedAnalysis = new ProjectDependencyAnalysis(project.getArtifact());
    for (ProjectDependencyAnalysis analysis : analyses) {
      mergedAnalysis.getArtifactsIdentifiers().addAll(analysis.getArtifactsIdentifiers());
      mergedAnalysis.getClassDependencies().putAll(analysis.getClassDependencies());
    }
    return mergedAnalysis;
  }

}
