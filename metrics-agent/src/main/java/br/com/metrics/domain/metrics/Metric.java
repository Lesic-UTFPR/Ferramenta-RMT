package br.com.metrics.domain.metrics;

import java.nio.file.Path;

import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public enum Metric {
	DEPTH_OF_INHERITANCE_TREE {
		@Override
		public int calculate(CKReport ckReport) {
			return ckReport.all().stream().mapToInt(CKNumber::getDit).sum();
		}
	},
	CYCLOMATIC_COMPLEXITY {
		@Override
		public int calculate(CKReport ckReport) {
			return ckReport.all().stream().mapToInt(CKNumber::getWmc).sum();
		}
	},
	LINES_OF_CODE {
		@Override
		public int calculate(CKReport ckReport) {
			return ckReport.all().stream().mapToInt(CKNumber::getLoc).sum();
		}
	};

	public int calculate(CKReport ckReport) {
		throw new IllegalStateException();
	}

	public int calculate(Path projectPath) {
		throw new IllegalStateException();
	}

	public boolean isCkReportBased() {
		return true;
	}

}
