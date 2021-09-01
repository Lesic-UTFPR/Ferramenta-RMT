package br.com.intermediary.managers.projects;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import br.com.messages.members.Member;

@Stateless
public class EvaluationsManagerImpl implements EvaluationsManager {

	private static final long serialVersionUID = 1L;

	@Asynchronous
	@Override
	public void start(EvaluatonResponseHandler responseHandler, Member member, String projectId) {
		
	}

}
