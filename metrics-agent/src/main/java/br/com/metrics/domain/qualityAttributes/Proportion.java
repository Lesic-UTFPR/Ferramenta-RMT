package br.com.metrics.domain.qualityAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Proportion {
	INVERSE {
		@Override
		public BigDecimal calculateResult(int original, int refactored) {
			return BigDecimal.valueOf(original, 2).multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(refactored, 2), RoundingMode.HALF_EVEN)
					.subtract(BigDecimal.valueOf(100));
		}
	},
	DIRECT {
		@Override
		public BigDecimal calculateResult(int original, int refactored) {
			return BigDecimal.valueOf(refactored, 2).multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(original, 2), RoundingMode.HALF_EVEN).subtract(BigDecimal.valueOf(100));
		}
	};

	public abstract BigDecimal calculateResult(int original, int refactored);

}
