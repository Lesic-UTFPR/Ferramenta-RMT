package br.com.intermediary.managers.projects.exceptions.evaluation;

import br.com.intermediary.managers.projects.exceptions.ManagerException;
import br.com.messages.requests.evaluation.exceptions.EvaluationExceptionCode;

public class ProjectEvaluationException extends ManagerException {

	private static final long serialVersionUID = 1L;

	public ProjectEvaluationException(EvaluationExceptionCode code) {
		super(code.name());
	}
}
