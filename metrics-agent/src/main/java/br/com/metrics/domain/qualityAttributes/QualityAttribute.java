package br.com.metrics.domain.qualityAttributes;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.metrics.domain.metrics.Metric;

public enum QualityAttribute {
	MAINTAINABILITY(new QualityAttributeMetric(Metric.DEPTH_OF_INHERITANCE_TREE),
			new QualityAttributeMetric(Metric.CYCLOMATIC_COMPLEXITY, Proportion.INVERSE),
			new QualityAttributeMetric(Metric.LINES_OF_CODE, Proportion.INVERSE)),
	RELIABILITY(new QualityAttributeMetric(Metric.CYCLOMATIC_COMPLEXITY, Proportion.INVERSE),
			new QualityAttributeMetric(Metric.LINES_OF_CODE, Proportion.INVERSE)),
	REUSABILITY(new QualityAttributeMetric(Metric.DEPTH_OF_INHERITANCE_TREE),
			new QualityAttributeMetric(Metric.LINES_OF_CODE, Proportion.INVERSE));

	private final Collection<QualityAttributeMetric> metrics;

	private QualityAttribute(QualityAttributeMetric... metrics) {
		this.metrics = Stream.of(metrics).collect(Collectors.toList());
	}

	public Collection<QualityAttributeMetric> getQualityAttributeMetric() {
		return metrics;
	}

}
