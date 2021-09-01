package br.com.metrics.repositories.project;

import java.io.Serializable;
import java.util.Optional;

import br.com.messages.projects.Project;
import br.com.metrics.domain.files.FileRepositoryCollections;

public interface ProjectsReadonlyRepository extends Serializable {

	Optional<Project> get(FileRepositoryCollections collection, String id);

}
