package br.com.metrics.domain.qualityAttributes;

import br.com.metrics.domain.metrics.Metric;

public class QualityAttributeMetric {

	private final Metric metrics;

	private final Proportion proportion;

	public QualityAttributeMetric(Metric metric) {
		this(metric, Proportion.DIRECT);
	}

	public QualityAttributeMetric(Metric metric, Proportion proportion) {
		this.metrics = metric;
		this.proportion = proportion;
	}

	public Metric getMetrics() {
		return metrics;
	}

	public Proportion getProportion() {
		return proportion;
	}
	
}
