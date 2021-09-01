package br.com.intermediary.managers.projects;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import javax.ejb.Local;

import br.com.messages.projects.Project;

@Local
public interface ProjectsPool extends Serializable {

	void register(Project project);

	Collection<Project> getAll();

	Optional<Project> getById(String id);

	boolean isRegistered(String projectId);

}
