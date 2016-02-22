package croquette.graph.maven.analyze;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "analyze-aggregate", aggregator = true, inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.TEST_COMPILE)
public class AggregatingAnalyzeMojo extends AbstractAnalyzeMojo {

}
