package br.com.detection.repositories.project;

import java.io.Serializable;
import java.util.Optional;

import br.com.detection.domain.files.FileRepositoryCollections;
import br.com.messages.projects.Project;

public interface ProjectsReadonlyRepository extends Serializable {

	Optional<Project> get(FileRepositoryCollections collection, String id);

}
