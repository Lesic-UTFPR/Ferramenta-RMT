package br.com.detection.domain.methods.cinneide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;

import br.com.detection.domain.methods.DetectionMethod;
import br.com.detection.domain.methods.cinneide.executors.Cinneide2000Executor;
import br.com.detection.domain.methods.cinneide.executors.Cinneide2000FactoryMethodExecutor;
import br.com.detection.domain.methods.cinneide.executors.Cinneide2000SingletonExecutor;
import br.com.detection.domain.methods.cinneide.verifiers.Cinneide2000FactoryMethodVerifier;
import br.com.detection.domain.methods.cinneide.verifiers.Cinneide2000SingletonVerifier;
import br.com.detection.domain.methods.cinneide.verifiers.Cinneide2000Verifier;
import br.com.detection.domain.methods.details.Author;
import br.com.detection.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

@Stateless
public class Cinneide2000 implements DetectionMethod, AbstractSyntaxTreeDependent {

	private final String title;

	private final int year;

	private final Set<DesignPattern> designPatterns = new HashSet<>();

	private final List<Author> authors = new ArrayList<>();

	public Cinneide2000() {
		this.title = "Automated Application of Design Patterns: A Refactoring Approach";
		this.year = 2000;

		this.authors.add(new Author("Mel Ó Cinnéide"));

		this.designPatterns.add(DesignPattern.SINGLETON);
		this.designPatterns.add(DesignPattern.FACTORY_METHOD);
	}

	@Override
	public Collection<RefactoringCandidate> extractCandidates(DataHandler dataHandler) {

		final Collection<RefactoringCandidate> candidates = this.getVerifiers().flatMap(v -> {
			return v.retrieveCandidatesFrom(this.toReference(), dataHandler).stream();
		}).collect(Collectors.toList());

		System.out.println("Printing Cinneide");
		System.out.println(candidates);
		System.out.println("=============================");

		return candidates;
	}

	// Add new verifier here
	private Stream<Cinneide2000Verifier> getVerifiers() {
//		return Stream.of(new Cinneide2000SingletonVerifier(), new Cinneide2000FactoryMethodVerifier());
		return Stream.of(new Cinneide2000SingletonVerifier());
	}

	@Override
	public void refactor(DataHandler dataHandler, RefactoringCandidate candidate) {

		this.getExecutors().filter(e -> e.isApplicable(candidate)).findFirst()
				.orElseThrow(IllegalArgumentException::new).refactor(candidate, dataHandler);
	}

	// Add new executors here
	private Stream<Cinneide2000Executor> getExecutors() {
//		return Stream.of(new Cinneide2000SingletonExecutor(), new Cinneide2000FactoryMethodExecutor());
		return Stream.of(new Cinneide2000SingletonExecutor());
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public Set<DesignPattern> getDesignPatterns() {
		return designPatterns;
	}

	@Override
	public List<Author> getAuthors() {
		return authors;
	}

	@Override
	public Reference toReference() {
		return new Reference(title, year, authors.stream().map(Author::getName).collect(Collectors.toList()));
	}

}
