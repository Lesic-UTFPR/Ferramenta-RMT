package br.com.metrics.domain.metrics.processor;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import br.com.messages.utils.Logger;
import br.com.metrics.domain.metrics.ast.ClassInfo;
import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public class MetricsProcessorsExecutor extends FileASTRequestor {

	private CKReport report;
	private Callable<List<MetricProcessor>> metrics;

	private static Logger log = Logger.get();

	public MetricsProcessorsExecutor(Callable<List<MetricProcessor>> metrics) {
		this.metrics = metrics;
		this.report = new CKReport();
	}

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit cu) {

		CKNumber result = null;

		try {

			ClassInfo info = new ClassInfo();
			cu.accept(info);
			if (info.getClassName() == null)
				return;

			result = new CKNumber(sourceFilePath, info.getClassName(), info.getType());

			int loc = new LOCCalculator().calculate(new FileInputStream(sourceFilePath));
			result.setLoc(loc);

			for (MetricProcessor visitor : metrics.call()) {
				visitor.execute(cu, result, report);
				visitor.setResult(result);
			}
			log.i(this, ToStringBuilder.reflectionToString(result));
			report.add(result);
		} catch (Exception e) {
			if (result != null)
				result.error();
			log.e(this, "error in " + sourceFilePath, e);
		}
	}

	public CKReport getReport() {
		return report;
	}

}
