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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.ClassAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import croquette.graph.maven.analyze.ArtifactUtils;
import croquette.graph.maven.analyze.analysis.ClassAnalysis;
import croquette.graph.maven.analyze.analysis.ClassIdentifier;
import croquette.graph.maven.analyze.analysis.InternalClassAnalysis;
import croquette.graph.maven.analyze.analysis.ProjectDependencyAnalysis;

@Component(role = ProjectDependencyAnalyzer.class)
public class DefaultProjectDependencyAnalyzer implements ProjectDependencyAnalyzer {
	// fields -----------------------------------------------------------------

	/**
	 * ClassAnalyzer
	 */
	@Requirement(role = ClassAnalyzer.class, hint = "class")
	private ClassAnalyzer classAnalyzer;

	/**
	 * DependencyAnalyzer
	 */
	@Requirement
	private InternalClassDependencyAnalyzer dependencyAnalyzer;

	// ProjectDependencyAnalyzer methods --------------------------------------

	/*
	 * @see
	 * org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer#
	 * analyze(org.apache.maven.project.MavenProject )
	 */
	public ProjectDependencyAnalysis analyze(MavenProject project) throws ProjectDependencyAnalyzerException {

		try {
			Map<String, Set<String>> artifactClassMap = buildArtifactClassMap(project);
			List<InternalClassAnalysis> dependencyClasses = buildDependencyClasses(project);

			Map<String, ClassAnalysis> classAnalysisMap = buildProjectAnalysis(artifactClassMap, dependencyClasses);
			Set<String> dependencies = artifactClassMap.keySet();
			return new ProjectDependencyAnalysis(project.getArtifact(), dependencies, classAnalysisMap);
		} catch (IOException exception) {
			throw new ProjectDependencyAnalyzerException("Cannot analyze dependencies", exception);
		}
	}

	private Map<String, ClassAnalysis> buildProjectAnalysis(Map<String, Set<String>> artifactClassMap,
			List<InternalClassAnalysis> dependencyClasses) {
		Map<String, ClassAnalysis> classAnalysisMap = new HashMap<String, ClassAnalysis>();

		for (InternalClassAnalysis internalClassAnalysis : dependencyClasses) {
			ClassAnalysis classAnalysis = new ClassAnalysis(internalClassAnalysis.getArtifactIdentifier(),
					internalClassAnalysis.getClassName());
			classAnalysisMap.put(classAnalysis.getClassName(), classAnalysis);
			for (String dependencyClassName : internalClassAnalysis.getDependencies()) {
				String artifactIdentifier = findArtifactForClassName(artifactClassMap, dependencyClassName);
				if (artifactIdentifier != null) {
					ClassIdentifier dependencyClassIdentifier = new ClassIdentifier(artifactIdentifier,
							dependencyClassName);
					classAnalysis.getDependencies().add(dependencyClassIdentifier);
				}
			}
		}
		return classAnalysisMap;
	}

	// private methods --------------------------------------------------------

	private Map<String, Set<String>> buildArtifactClassMap(MavenProject project) throws IOException {
		Map<String, Set<String>> artifactClassMap = new LinkedHashMap<String, Set<String>>();

		@SuppressWarnings("unchecked")
		Set<Artifact> dependencyArtifacts = project.getArtifacts();

		for (Artifact artifact : dependencyArtifacts) {
			String artifactIdentifier = ArtifactUtils.versionLess(artifact);
			File file = artifact.getFile();

			if (file != null && file.getName().endsWith(".jar")) {
				// optimized solution for the jar case
				JarFile jarFile = new JarFile(file);

				try {
					Enumeration<JarEntry> jarEntries = jarFile.entries();

					Set<String> classes = new HashSet<String>();

					while (jarEntries.hasMoreElements()) {
						String entry = jarEntries.nextElement().getName();
						if (entry.endsWith(".class")) {
							String className = entry.replace('/', '.');
							className = className.substring(0, className.length() - ".class".length());
							classes.add(className);
						}
					}

					artifactClassMap.put(artifactIdentifier, classes);
				} finally {
					try {
						jarFile.close();
					} catch (IOException ignore) {
						// ingore
					}
				}
			} else if (file != null && file.isDirectory()) {
				URL url = file.toURI().toURL();
				Set<String> classes = classAnalyzer.analyze(url);

				artifactClassMap.put(artifactIdentifier, classes);
			}
		}

		return artifactClassMap;
	}

	protected List<InternalClassAnalysis> buildDependencyClasses(MavenProject project) throws IOException {
		List<InternalClassAnalysis> dependencyClasses = new ArrayList<InternalClassAnalysis>();

		String outputDirectory = project.getBuild().getOutputDirectory();
		dependencyClasses.addAll(buildDependencyClasses(project.getArtifact(), outputDirectory));

		String testOutputDirectory = project.getBuild().getTestOutputDirectory();
		dependencyClasses.addAll(buildDependencyClasses(project.getArtifact(), testOutputDirectory));

		return dependencyClasses;
	}

	private List<InternalClassAnalysis> buildDependencyClasses(Artifact artifact, String path) throws IOException {
		URL url = new File(path).toURI().toURL();
		List<InternalClassAnalysis> analysisResult = dependencyAnalyzer.analyze(artifact, url);
		return analysisResult;
	}

	protected String findArtifactForClassName(Map<String, Set<String>> artifactClassMap, String className) {
		for (Map.Entry<String, Set<String>> entry : artifactClassMap.entrySet()) {
			if (entry.getValue().contains(className)) {
				return entry.getKey();
			}
		}

		return null;
	}
}
