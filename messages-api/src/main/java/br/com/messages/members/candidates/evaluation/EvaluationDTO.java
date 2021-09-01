package br.com.messages.members.candidates.evaluation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import br.com.messages.members.metrics.QualityAttributeResultDTO;

public class EvaluationDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Collection<QualityAttributeResultDTO> qualityAttributes = new ArrayList<>();

	public EvaluationDTO() {
	}

	public EvaluationDTO(Collection<QualityAttributeResultDTO> qualityAttributes) {
		this.qualityAttributes.addAll(qualityAttributes);
	}

	public Collection<QualityAttributeResultDTO> getQualityAttributes() {
		return qualityAttributes;
	}
	
	public BigDecimal getQualityAttributeValueByName(String qualityAttribute) {
		return this.qualityAttributes.stream().filter(qa -> qa.getQualityAttributeName().equals(qualityAttribute))
				.map(QualityAttributeResultDTO::getChangePercentage).findFirst().orElse(BigDecimal.ZERO);
	}

}
