package croquette.graph.maven.analyze.analysis;

import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

public class ProjectDependencyAnalysis {

	private final Artifact artifact;

	/**
	 * Dependency map Class -> classes
	 */
	private final Map<String, ClassAnalysis> classDependencies;

	private final Set<String> artifactsIdentifiers;

	public ProjectDependencyAnalysis(Artifact artifact, Set<String> artifactsIdentifiers,
			Map<String, ClassAnalysis> classDependencies) {
		this.artifact = artifact;
		this.artifactsIdentifiers = artifactsIdentifiers;
		this.classDependencies = classDependencies;
	}

	public Map<String, ClassAnalysis> getClassDependencies() {
		return classDependencies;
	}

	public Set<String> getArtifactsIdentifiers() {
		return artifactsIdentifiers;
	}

	public Artifact getArtifact() {
		return artifact;
	}

}
