package br.com.intermediary.managers.projects.exceptions.registration;

import br.com.intermediary.managers.projects.exceptions.ManagerException;
import br.com.messages.requests.registration.exceptions.RegistrationExceptionCode;

public class ProjectRegistrationException extends ManagerException{

	private static final long serialVersionUID = 1L;

	public ProjectRegistrationException(RegistrationExceptionCode code) {
		super(code.name());
	}
}
