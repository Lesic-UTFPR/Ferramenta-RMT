package br.com.metrics.repositories.project;

import br.com.messages.projects.Project;
import br.com.metrics.domain.files.FileRepositoryCollections;

public interface ProjectsRepository extends ProjectsReadonlyRepository {

	void put(FileRepositoryCollections collection, Project project);

	void remove(FileRepositoryCollections collection, Project project);

}
