package br.com.intermediary.managers.projects.exceptions.registration;

import br.com.messages.requests.registration.exceptions.RegistrationExceptionCode;

public class InvalidProjectNameException extends ProjectRegistrationException{

	private static final long serialVersionUID = 1L;

	public InvalidProjectNameException() {
		super(RegistrationExceptionCode.REG_INVALID_PROJECT_DESCRIPTION);
	}
}
