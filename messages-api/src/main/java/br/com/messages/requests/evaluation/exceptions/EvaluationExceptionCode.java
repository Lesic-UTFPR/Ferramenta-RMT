package br.com.messages.requests.evaluation.exceptions;

import java.util.stream.Stream;

public enum EvaluationExceptionCode {
	EVAL_UNREGISTERED_PROJECT, EVAL_PROJECT_ALREADY_PROCESSING, EVAL_THERE_IS_NO_ALIVE_DETECTOR;

	public static EvaluationExceptionCode getFromStr(String str) {
		return Stream.of(EvaluationExceptionCode.values()).filter(p -> p.name().equals(str)).findAny().orElseThrow(
				() -> new IllegalArgumentException(String.format("Código de exceção não mapeado (%s)", str)));
	}
}
