package br.com.detection.domain.methods;

import java.util.List;

import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

public interface RefactoringCandidatesVerifier {

	public List<RefactoringCandidate> retrieveCandidatesFrom(Reference reference, DataHandler dataHandler);

}
