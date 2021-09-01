package br.com.messages.members.clients;

import java.io.Serializable;

public class Registration implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String projectId;

	private final String projectName;

	private final byte[] projectContent;

	public Registration() {
		this("", "", null);
	}

	public Registration(String projectId, String projectName) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectContent = null;
	}

	public Registration(String projectId, String projectName, byte[] projectContent) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectContent = projectContent;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public byte[] getProjectContent() {
		return projectContent;
	}

}
