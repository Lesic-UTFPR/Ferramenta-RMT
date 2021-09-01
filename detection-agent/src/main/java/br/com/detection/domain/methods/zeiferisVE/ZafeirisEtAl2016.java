package br.com.detection.domain.methods.zeiferisVE;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;

import br.com.detection.domain.methods.DetectionMethod;
import br.com.detection.domain.methods.details.Author;
import br.com.detection.domain.methods.zeiferisVE.executors.ZafeirisEtAl2016Executor;
import br.com.detection.domain.methods.zeiferisVE.verifiers.ZafeirisEtAl2016Verifier;
import br.com.detection.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

@Stateless
public class ZafeirisEtAl2016 implements DetectionMethod, AbstractSyntaxTreeDependent {

	private final String title;

	private final int year;

	private final Set<DesignPattern> designPatterns = new HashSet<>();

	private final List<Author> authors = new ArrayList<>();

	public ZafeirisEtAl2016() {
		this.title = "Automated refactoring of super-class method invocations to the Template Method design pattern";
		this.year = 2016;

		this.authors.add(new Author("E. A. Giakoumakis"));
		this.authors.add(new Author("N. A. Diamantidis"));
		this.authors.add(new Author("Sotiris H. Poulias"));
		this.authors.add(new Author("Vassilis E. Zafeiris"));

		this.designPatterns.add(DesignPattern.TEMPLATE_METHOD);
	}

	@Override
	public Collection<RefactoringCandidate> extractCandidates(DataHandler dataHandler) {
		try {
			return new ZafeirisEtAl2016Verifier().retrieveCandidatesFrom(this.toReference(), dataHandler).stream()
					.map(RefactoringCandidate.class::cast).collect(Collectors.toList());
		} catch (MalformedURLException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void refactor(DataHandler dataHandler, RefactoringCandidate candidate) {
		new ZafeirisEtAl2016Executor().refactor((ZafeirisEtAl2016Canditate) candidate, dataHandler);
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
