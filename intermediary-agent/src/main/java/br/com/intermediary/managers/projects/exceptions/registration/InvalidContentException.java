package br.com.intermediary.managers.projects.exceptions.registration;

import br.com.messages.requests.registration.exceptions.RegistrationExceptionCode;

public class InvalidContentException extends ProjectRegistrationException {

	private static final long serialVersionUID = 1L;

	public InvalidContentException() {
		super(RegistrationExceptionCode.REG_INVALID_PROJECT_CONTENT);
	}

}
