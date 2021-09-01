package br.com.detection.domain.methods.zeiferisVE.verifiers;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import br.com.detection.domain.methods.zeiferisVE.preconditions.ExtractMethodPreconditions;
import br.com.detection.domain.methods.zeiferisVE.preconditions.SiblingPreconditions;
import br.com.detection.domain.methods.zeiferisVE.preconditions.SuperInvocationPreconditions;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;

public class ZafeirisEtAl2016Verifier {

	private final AstHandler astHandler = new AstHandler();

	private final SuperInvocationPreconditions superInvocationPreconditions = new SuperInvocationPreconditions();

	private final ExtractMethodPreconditions extractMethodPreconditions = new ExtractMethodPreconditions();

	private final SiblingPreconditions siblingPreconditions = new SiblingPreconditions();

	private Collection<CompilationUnit> getParsedClasses(DataHandler dataHandler) {
		return dataHandler.getParsedFiles().stream().map(CompilationUnit.class::cast).collect(Collectors.toList());
	}

	public List<ZafeirisEtAl2016Canditate> retrieveCandidatesFrom(Reference reference, DataHandler dataHandler)
			throws MalformedURLException, FileNotFoundException {

		final List<ZafeirisEtAl2016Canditate> candidates = this.retrieveCandidates(reference, dataHandler);

		for (MethodDeclaration overridenMethod : candidates.stream().map(ZafeirisEtAl2016Canditate::getOverridenMethod)
				.collect(Collectors.toList())) {

			final Collection<ZafeirisEtAl2016Canditate> canditadesOfSameOverridenMethod = candidates.stream()
					.filter(c -> c.getOverridenMethod().equals(overridenMethod)).collect(Collectors.toList());

			if (siblingPreconditions.violates(canditadesOfSameOverridenMethod)) {
				candidates.removeAll(candidates.stream().filter(c -> c.getOverridenMethod().equals(overridenMethod))
						.collect(Collectors.toList()));
			}
		}
		return candidates;
	}

	private List<ZafeirisEtAl2016Canditate> retrieveCandidates(Reference reference, DataHandler dataHandler) {
		final List<ZafeirisEtAl2016Canditate> candidates = new ArrayList<>();

		final Collection<CompilationUnit> allCUnits = this.getParsedClasses(dataHandler);

		for (Path file : dataHandler.getFiles()) {
			final CompilationUnit parsedClazz = (CompilationUnit) dataHandler.parseFile(file);

			if (parsedClazz == null) {
				continue;
			}
			
			final Optional<CompilationUnit> parent = this.astHandler.getParent(parsedClazz, allCUnits);

			this.retrieveCandidate(reference, file, parsedClazz, parent).ifPresent(candidates::add);

		}

		return candidates;
	}

	private Optional<ZafeirisEtAl2016Canditate> retrieveCandidate(Reference reference, Path file, CompilationUnit cUnit,
			Optional<CompilationUnit> parent) {

		if (this.violatesClassPreconditions(cUnit, parent)) {
			return Optional.empty();
		}

		final Collection<MethodDeclaration> methods = this.astHandler.getMethods(cUnit);

		for (MethodDeclaration method : methods.stream().filter(m -> !m.isConstructorDeclaration() && !m.isStatic())
				.collect(Collectors.toList())) {

			final Collection<SuperExpr> superCalls = this.astHandler.getSuperCalls(method);

			if (superInvocationPreconditions.violatesAmountOfSuperCallsOrName(method, superCalls)) {
				continue;
			}

			final SuperExpr superCall = superCalls.stream().findFirst().get();

			final MethodDeclaration overridenMethod = this.astHandler.retrieveOverridenMethod(cUnit, parent.get(),
					method);

			if (overridenMethod == null) {
				continue;
			}

			if (!this.superInvocationPreconditions.isOverriddenMethodValid(overridenMethod, method)
					|| !extractMethodPreconditions.isValid(overridenMethod, method, superCall)) {
				continue;
			}

			return Optional.of(this.createCandidate(reference, file, cUnit, overridenMethod, method, superCall));

		}
		return Optional.empty();
	}

	private ZafeirisEtAl2016Canditate createCandidate(Reference reference, Path file, CompilationUnit cUnit,
			MethodDeclaration overridenMethod, MethodDeclaration method, SuperExpr superCall) {
		final PackageDeclaration pkgDcl = this.astHandler.getPackageDeclaration(cUnit);

		final ClassOrInterfaceDeclaration classDcl = this.astHandler.getClassOrInterfaceDeclaration(cUnit).get();

		return new ZafeirisEtAl2016Canditate(reference, file, cUnit, pkgDcl, classDcl, overridenMethod, method,
				superCall);
	}

	private boolean violatesClassPreconditions(CompilationUnit cUnit, Optional<CompilationUnit> parent) {
		return !parent.isPresent();

	}

}
