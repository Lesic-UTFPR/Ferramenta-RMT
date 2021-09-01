package br.com.detection.methods;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import br.com.detection.domain.files.FileRepositoryCollections;
import br.com.detection.methods.dataExtractions.forks.DataExtractionFork;
import br.com.detection.repositories.project.ProjectsRepository;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;

@Singleton
public class DetectionMethodsManagerImpl implements DetectionMethodsManager {

	private final ProjectsRepository projectsRepository;

	private final Collection<DataExtractionFork> dataExtractionForks;

	private final Map<String, Collection<RefactoringCandidate>> candidatesOfProjects = new HashMap<>();

	public DetectionMethodsManagerImpl() {
		this.projectsRepository = null;
		this.dataExtractionForks = null;
	}

	@Inject
	public DetectionMethodsManagerImpl(ProjectsRepository projectsRepository,
			@Any Instance<DataExtractionFork> dataExtractionForks) {
		this.projectsRepository = projectsRepository;
		this.dataExtractionForks = StreamSupport.stream(dataExtractionForks.spliterator(), false).collect(toList());
	}
	/*
	 * Everything starts and ends here...
	 */
	@Lock(LockType.WRITE)
	@Override
	public Collection<RefactoringCandidate> extractCandidates(String projectId) {

		final Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
				.orElseThrow(IllegalArgumentException::new);

		if (!this.candidatesOfProjects.containsKey(project.getId())) {
			this.candidatesOfProjects.put(project.getId(), new ArrayList<>());
		}
		this.candidatesOfProjects.get(project.getId()).clear();
		this.candidatesOfProjects.get(project.getId()).addAll(this.dataExtractionForks.stream()
				.flatMap(f -> f.findCandidates(project).stream()).collect(Collectors.toList()));

		return this.candidatesOfProjects.get(project.getId());
	}

	@Lock(LockType.WRITE)
	@Override
	public String refactor(String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns) {

		Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
				.orElseThrow(IllegalArgumentException::new);

		Optional.ofNullable(this.candidatesOfProjects.get(projectId)).orElseThrow(IllegalStateException::new);

		for (RefactoringCandidate candidate : eligiblePatterns.stream()
				.filter(dto -> this.isCandidateProcessed(projectId, dto))
				.map(dto -> this.parseCandidateDTO(projectId, dto).get()).collect(Collectors.toList())) {

			final Optional<DataExtractionFork> fork = this.dataExtractionForks.stream()
					.filter(f -> f.belongsTo(candidate.getReference())).findFirst();

			if (fork.isPresent()) {
				final String refactoredProjectId = fork.get().refactor(project, candidate);

				return this.projectsRepository
						.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredProjectId)
						.orElseThrow(IllegalArgumentException::new).getId();
			} 
		}

		throw new IllegalArgumentException();
	}

	private boolean isCandidateProcessed(String projectId, RefactoringCandidadeDTO rc) {
		final boolean processed = this.candidatesOfProjects.get(projectId).stream()
				.anyMatch(c -> {
					return c.getId().equals(rc.getId());
				});

		return processed;
	}

	private Optional<RefactoringCandidate> parseCandidateDTO(String projectId, RefactoringCandidadeDTO dto) {
		final Optional<RefactoringCandidate> candidate = this.candidatesOfProjects.get(projectId).stream()
				.filter(rc -> {
					return dto.getId().equals(rc.getId());
				}).findFirst();

		return candidate;
	}

	@Override
	public Collection<Reference> getReferences() {
		return this.dataExtractionForks.stream().map(DataExtractionFork::getReferences).flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

}
