package br.com.metrics.processors.qualityAttributes.forks;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;

import br.com.metrics.domain.metrics.Metric;
import br.com.metrics.domain.metrics.report.CK;
import br.com.metrics.domain.metrics.report.CKReport;

@Stateless
public class CkReportProcessorFork implements QualityAttributeProcessorFork {

	private final Collection<Metric> metrics = Stream.of(Metric.values()).filter(Metric::isCkReportBased)
			.collect(Collectors.toList());

	@Override
	public Collection<Metric> getMetrics() {
		return metrics;
	}

	@Override
	public Map<Metric, Integer> process(Path projectPath) {
		final CKReport report = new CK().calculate(projectPath.toFile().getAbsolutePath());
		
		final Map<Metric, Integer> projectMetrics = new HashMap<>();
		for(Metric m : metrics) {
			projectMetrics.put(m, m.calculate(report));
		}
		
		return projectMetrics;
	}

}
