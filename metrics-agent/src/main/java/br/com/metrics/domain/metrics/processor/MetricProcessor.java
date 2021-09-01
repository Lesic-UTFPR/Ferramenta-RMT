package br.com.metrics.domain.metrics.processor;

import org.eclipse.jdt.core.dom.CompilationUnit;

import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public interface MetricProcessor {

	void execute(CompilationUnit cu, CKNumber result, CKReport report);
	
	void setResult(CKNumber result);
}
