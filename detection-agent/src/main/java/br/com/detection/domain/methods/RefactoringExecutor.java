package br.com.detection.domain.methods;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;

import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;

public interface RefactoringExecutor {

	boolean isApplicable(RefactoringCandidate candidate);

	void refactor(RefactoringCandidate candidate, DataHandler dataHandler);
	
	default Collection<CompilationUnit> getParsedClasses(DataHandler dataHandler) {
		return dataHandler.getParsedFiles().stream().map(CompilationUnit.class::cast).collect(Collectors.toList());
	}
	
	default void writeCanges(CompilationUnit cUnit, Path file) {
		try (FileWriter fileWriter = new FileWriter(file.toFile())) {
			fileWriter.write(cUnit.toString());
			fileWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
