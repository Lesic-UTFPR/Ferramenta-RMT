package br.com.metrics.domain.metrics.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import com.google.common.collect.Lists;

import br.com.messages.utils.Logger;
import br.com.metrics.domain.metrics.processor.DITProcessor;
import br.com.metrics.domain.metrics.processor.FileUtils;
import br.com.metrics.domain.metrics.processor.LCOMProcessor;
import br.com.metrics.domain.metrics.processor.MetricProcessor;
import br.com.metrics.domain.metrics.processor.MetricsProcessorsExecutor;
import br.com.metrics.domain.metrics.processor.NOCExtras;
import br.com.metrics.domain.metrics.processor.NOCProcessor;
import br.com.metrics.domain.metrics.processor.WMCProcessor;

/**
 * https://github.com/mauricioaniche/ck
 * */
public class CK {

	private static final int MAX_AT_ONCE;

	static {
		String jdtMax = System.getProperty("jdt.max");
		if (jdtMax != null) {
			MAX_AT_ONCE = Integer.parseInt(jdtMax);
		} else {
			long maxMemory = Runtime.getRuntime().maxMemory() / (1 << 20); // in MiB

			if (maxMemory >= 2000)
				MAX_AT_ONCE = 400;
			else if (maxMemory >= 1500)
				MAX_AT_ONCE = 300;
			else if (maxMemory >= 1000)
				MAX_AT_ONCE = 200;
			else if (maxMemory >= 500)
				MAX_AT_ONCE = 100;
			else
				MAX_AT_ONCE = 25;
		}
	}

	private final NOCExtras extras = new NOCExtras();

	public List<Callable<MetricProcessor>> pluggedMetrics = new ArrayList<>();
	private static Logger log = Logger.get();

	public CK plug(Callable<MetricProcessor> metric) {
		this.pluggedMetrics.add(metric);
		return this;
	}

	public CKReport calculate(String path) {
		String[] srcDirs = FileUtils.getAllDirs(path);
		String[] javaFiles = FileUtils.getAllJavaFiles(path);
		log.i(this, "Found " + javaFiles.length + " java files");

		MetricsProcessorsExecutor storage = new MetricsProcessorsExecutor(() -> metrics());

		List<List<String>> partitions = Lists.partition(Arrays.asList(javaFiles), MAX_AT_ONCE);
		log.i(this, "Max partition size: " + MAX_AT_ONCE + ", total partitions=" + partitions.size());

		for (List<String> partition : partitions) {
			log.i(this, "Next partition");
			ASTParser parser = ASTParser.newParser(AST.JLS8);

			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);

			Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);
			parser.setEnvironment(null, srcDirs, null, true);
			parser.createASTs(partition.toArray(new String[partition.size()]), null, new String[0], storage, null);
		}

		log.i(this, "Finished parsing");
		CKReport report = storage.getReport();
		extras.update(report);
		return report;
	}

	private List<MetricProcessor> metrics() {
		List<MetricProcessor> all = defaultMetrics();
		all.addAll(userMetrics());

		return all;
	}

	private List<MetricProcessor> defaultMetrics() {
		return new ArrayList<>(Arrays.asList(new DITProcessor(), new NOCProcessor(extras), new WMCProcessor(), new LCOMProcessor()));
	}

	private List<MetricProcessor> userMetrics() {
		try {
			List<MetricProcessor> userMetrics = new ArrayList<MetricProcessor>();

			for (Callable<MetricProcessor> metricToBeCreated : pluggedMetrics) {
				userMetrics.add(metricToBeCreated.call());
			}

			return userMetrics;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
