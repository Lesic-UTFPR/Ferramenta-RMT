package br.com.messages.members.metrics;

import java.math.BigDecimal;

public class QualityAttributeResultDTO implements QualityAttributeResult {

	private final String qualityAttributeName;

	private final BigDecimal changePercentage;

	public QualityAttributeResultDTO() {
		this(null, null);
	}

	public QualityAttributeResultDTO(String qualityAttributeName, BigDecimal changePercentage) {
		this.qualityAttributeName = qualityAttributeName;
		this.changePercentage = changePercentage;
	}

	@Override
	public String getQualityAttributeName() {
		return qualityAttributeName;
	}

	@Override
	public BigDecimal getChangePercentage() {
		return changePercentage;
	}

}
