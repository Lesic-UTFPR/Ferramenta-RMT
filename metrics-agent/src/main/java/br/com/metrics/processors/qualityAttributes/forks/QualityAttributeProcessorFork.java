package br.com.metrics.processors.qualityAttributes.forks;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import br.com.metrics.domain.metrics.Metric;

public interface QualityAttributeProcessorFork {
	
	Collection<Metric> getMetrics();
	
	Map<Metric, Integer> process(Path projectPath);

}
