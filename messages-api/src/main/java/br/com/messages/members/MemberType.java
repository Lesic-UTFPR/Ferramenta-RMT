package br.com.messages.members;

public enum MemberType {
	PATTERNS_SPOTS_DETECTOR("Detector"), PATTERNS_METRICS_EVALUATOR("Métricas"), PATTERNS_INCLUDER("Inclusor");

	private final String description;

	private MemberType(String description) {
		this.description = description;
	}

	public String get() {
		return description;
	}
}
