package br.com.intermediary.managers.projects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import br.com.messages.projects.Project;

@Singleton
public class ProjectsPoolImpl implements ProjectsPool {

	private static final long serialVersionUID = 1L;
	
	private final Set<Project> projects = new HashSet<>();

	@Lock(LockType.WRITE)
	@Override
	public void register(Project project) {
		this.projects.add(project);
	}

	@Lock(LockType.WRITE)
	@Override
	public Collection<Project> getAll() {
		return projects;
	}

	@Lock(LockType.WRITE)
	@Override
	public Optional<Project> getById(String id) {
		return this.getAll().stream().filter(p -> p.getId().equals(id)).findFirst();
	}

	@Lock(LockType.WRITE)
	@Override
	public boolean isRegistered(String projectId) {
		return this.getAll().stream().anyMatch(p -> p.getId().equals(projectId));
	}

}
