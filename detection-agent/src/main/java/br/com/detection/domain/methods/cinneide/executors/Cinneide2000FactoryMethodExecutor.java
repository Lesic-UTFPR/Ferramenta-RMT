package br.com.detection.domain.methods.cinneide.executors;

import java.util.Collection;

import com.github.javaparser.ast.CompilationUnit;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.cinneide.Cinneide2000FactoryMethodCandidate;
import br.com.detection.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.domain.methods.cinneide.minitransformations.PartialAbstraction;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;

public class Cinneide2000FactoryMethodExecutor implements Cinneide2000Executor{

	private final AstHandler astHandler = new AstHandler();
	private final PartialAbstraction pa = new PartialAbstraction();
	
	@Override
	public boolean isApplicable(RefactoringCandidate candidate) {
		return candidate instanceof Cinneide2000FactoryMethodCandidate
				&& DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
	}

	@Override
	public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {
		final Cinneide2000FactoryMethodCandidate cinneidCandidate = (Cinneide2000FactoryMethodCandidate) candidate;

		final Collection<CompilationUnit> allClasses = pa.getParsedClasses(dataHandler);
		final CompilationUnit baseCu = pa.updateBaseCompilationUnit(allClasses, cinneidCandidate);		
	}

}
