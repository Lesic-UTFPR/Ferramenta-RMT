package br.com.intermediary.managers.projects;

import java.io.Serializable;

import javax.ejb.Local;

import br.com.messages.members.Member;

@Local
public interface EvaluationsManager extends Serializable {

	void start(EvaluatonResponseHandler responseHandler,Member member, String projectId);

}
