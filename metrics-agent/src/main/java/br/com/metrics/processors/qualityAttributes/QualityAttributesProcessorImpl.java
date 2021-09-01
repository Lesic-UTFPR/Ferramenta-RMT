package br.com.metrics.processors.qualityAttributes;

import static java.util.stream.Collectors.toList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;
import br.com.metrics.domain.metrics.Metric;
import br.com.metrics.domain.qualityAttributes.QualityAttribute;
import br.com.metrics.domain.qualityAttributes.QualityAttributeMetric;
import br.com.metrics.processors.qualityAttributes.forks.QualityAttributeProcessorFork;

@Singleton
public class QualityAttributesProcessorImpl implements QualityAttributesProcessor {

	private final Collection<QualityAttributeProcessorFork> qualityAttributeForks;

	public QualityAttributesProcessorImpl() {
		this.qualityAttributeForks = new ArrayList<>();
	}

	@Inject
	public QualityAttributesProcessorImpl(@Any Instance<QualityAttributeProcessorFork> qualityAttributeForks) {
		this.qualityAttributeForks = StreamSupport.stream(qualityAttributeForks.spliterator(), false).collect(toList());
	}

	@Lock(LockType.WRITE)
	@Override
	public Collection<QualityAttributeResult> extract(Project project, Project refactoredProject) {
		try {

			final Map<Metric, Integer> projectMetricsResult = getMetrics(project);

			final Map<Metric, Integer> refactoredProjectMetricsResult = getMetrics(refactoredProject);

			return extract(projectMetricsResult, refactoredProjectMetricsResult);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<QualityAttributeResult> extract(Map<Metric, Integer> projectMetricsResult,
			Map<Metric, Integer> refactoredProjectMetricsResult) {

		final List<QualityAttributeResult> qualityAttributes = new ArrayList<>();
		for (QualityAttribute qualityAttribute : QualityAttribute.values()) {
			final BigDecimal result = calculateQualityAttributeResult(qualityAttribute.getQualityAttributeMetric(),
					projectMetricsResult, refactoredProjectMetricsResult);

			qualityAttributes.add(wrapResult(qualityAttribute, result));
		}

		return qualityAttributes;
	}

	private QualityAttributeResult wrapResult(QualityAttribute qualityAttribute, BigDecimal result) {
		return new QualityAttributeResult() {
			@Override
			public String getQualityAttributeName() {
				return qualityAttribute.name();
			}

			@Override
			public BigDecimal getChangePercentage() {
				return result;
			}
		};
	}

	private BigDecimal calculateQualityAttributeResult(Collection<QualityAttributeMetric> qaMetrics,
			Map<Metric, Integer> projectMetricsResult, Map<Metric, Integer> refactoredProjectMetricsResult) {

		final Map<Metric, BigDecimal> overallResults = new HashMap<>();
		for (QualityAttributeMetric m : qaMetrics) {

			final int originalMetric = projectMetricsResult.get(m.getMetrics());
			final int refactoredMetric = refactoredProjectMetricsResult.get(m.getMetrics());

			final BigDecimal result = Optional
					.ofNullable(m.getProportion().calculateResult(originalMetric, refactoredMetric))
					.orElse(BigDecimal.ZERO);

			overallResults.put(m.getMetrics(), result);
		}

		final BigDecimal totalPercentage = overallResults.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		int amountOfMetricsInvolved = overallResults.keySet().size();

		final BigDecimal average = amountOfMetricsInvolved == 0 ? BigDecimal.ZERO
				: totalPercentage.divide(BigDecimal.valueOf(amountOfMetricsInvolved), RoundingMode.HALF_EVEN);

		return average;
	}

	private Map<Metric, Integer> getMetrics(Project project) throws IOException {
		final Path projectPath = this.openProject(project);

		final Map<Metric, Integer> metricsResult = new HashMap<>();
		for (final QualityAttributeProcessorFork fork : this.qualityAttributeForks) {
			final Map<Metric, Integer> metricResult = fork.process(projectPath);

			metricResult.keySet().forEach(k -> metricsResult.put(k, metricResult.get(k)));
		}

		this.removeFile(projectPath);

		return metricsResult;
	}

	private Path openProject(Project project) throws IOException {
		final Path projectPath = Paths.get(project.getId());

		try (InputStream pIs = project.getStream()) {
			this.removeFile(projectPath);
			Files.createFile(projectPath);

			try (FileOutputStream pFos = new FileOutputStream(projectPath.toFile())) {
				IOUtils.copy(pIs, pFos);
			}

			ZipUtil.explode(projectPath.toFile());

			return projectPath;
		}
	}

	private void removeFile(Path p) throws IOException {
		if (Files.exists(p)) {
			org.apache.commons.io.FileUtils.forceDelete(p.toFile());
		}
	}

}
