package br.com.intermediary.managers.projects;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import br.com.messages.members.Member;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.metrics.QualityAttributeResultDTO;
import br.com.messages.projects.Project;

public interface ProjectsManager extends Serializable {

	void register(Project project);

	List<RefactoringCandidadeDTO> evaluate(Supplier<Member> detector, String projectId);

	String refactor(Supplier<Member> detector, String projectId, List<RefactoringCandidadeDTO> candidates);

	Collection<Project> getProjects();

	Optional<Project> getProjectById(String id);

	Collection<QualityAttributeResultDTO> evaluate(Supplier<Member> supplier, String projectId,
			String refactoredProjId);

}
