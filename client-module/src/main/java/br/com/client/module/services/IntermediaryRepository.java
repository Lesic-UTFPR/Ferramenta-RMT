package br.com.client.module.services;

import java.nio.file.Path;
import java.util.List;

import br.com.messages.members.candidates.RefactoringCandidadeDTO;

public interface IntermediaryRepository {

	byte[] refactor(String projectId, List<RefactoringCandidadeDTO> candidates) throws ServiceExeption;

	List<RefactoringCandidadeDTO> evaluate(String projectId) throws ServiceExeption;

	String register(String projectName, Path projectSrc) throws Exception;

}
