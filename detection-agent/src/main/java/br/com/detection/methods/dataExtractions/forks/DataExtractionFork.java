package br.com.detection.methods.dataExtractions.forks;

import java.util.Collection;

import br.com.detection.methods.dataExtractions.DataExtractionApproach;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;

public interface DataExtractionFork extends DataHandler {

	/**
	 * Retrieve the project and  call the extractCandidates
	 * to find out if there is any
	 * @param project
	 * @return A collection of refactoring Candidates
	 */
	Collection<RefactoringCandidate> findCandidates(Project project);

	/**
	 * Get a AST
	 * @return A AST
	 */
	DataExtractionApproach getExtractionApproach();

	String refactor(Project project, RefactoringCandidate candidate);

	boolean belongsTo(Reference methodReference);

	Collection<Reference> getReferences();

}
