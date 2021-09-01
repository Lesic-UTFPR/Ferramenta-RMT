package br.com.detection.methods.dataExtractions.forks;

import java.util.Collection;

import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

public interface DataExtractionDependent {

	Collection<RefactoringCandidate> extractCandidates(DataHandler dataHandler);

	void refactor(DataHandler dataHandler, RefactoringCandidate candidates);

	Reference toReference();

}
