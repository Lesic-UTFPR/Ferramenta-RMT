package br.com.messages.members.metrics;

import java.math.BigDecimal;

public interface QualityAttributeResult {
	
	String getQualityAttributeName();
	
	BigDecimal getChangePercentage();

}
