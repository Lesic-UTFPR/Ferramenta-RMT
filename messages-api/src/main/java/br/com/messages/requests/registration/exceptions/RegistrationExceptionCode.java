package br.com.messages.requests.registration.exceptions;

import java.util.stream.Stream;

public enum RegistrationExceptionCode {
	REG_INVALID_PROJECT_ID, REG_INVALID_PROJECT_DESCRIPTION, REG_INVALID_PROJECT_CONTENT;

	public static RegistrationExceptionCode getFromStr(String str) {
		return Stream.of(RegistrationExceptionCode.values()).filter(p -> p.name().equals(str)).findAny().orElseThrow(
				() -> new IllegalArgumentException(String.format("Código de exceção não mapeado (%s)", str)));
	}
}
