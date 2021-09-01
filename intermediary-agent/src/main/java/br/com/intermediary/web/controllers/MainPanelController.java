package br.com.intermediary.web.controllers;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import br.com.intermediary.managers.members.MembersManager;
import br.com.intermediary.managers.projects.ProjectsPool;
import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;

@ViewScoped
@ManagedBean
public class MainPanelController implements Serializable {

	private static final long serialVersionUID = 1L;

	private @Inject MembersManager membersManager;

	private @Inject ProjectsPool projectsPool;

	private Member selectedMember;

	public List<Member> getMembers() {
		return this.membersManager.getAliveMembers();
	}

	public List<Project> getProjects() {
		return this.projectsPool.getAll().stream().collect(Collectors.toList());
	}

	public boolean isADetectionMember(Member m) {
		return MemberType.PATTERNS_SPOTS_DETECTOR.equals(m.getMemberType());
	}

	public List<Reference> getReferences() {
		if (selectedMember == null) {
			return Collections.emptyList();
		}

		return this.membersManager.getDetectorReferences(selectedMember);
	}

	public Member getSelectedMember() {
		return selectedMember;
	}

	public void updateSelectedMember(Member selectedMember) {
		this.selectedMember = selectedMember;
	}

}
