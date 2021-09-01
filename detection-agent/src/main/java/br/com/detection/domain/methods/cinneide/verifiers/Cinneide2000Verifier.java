package br.com.detection.domain.methods.cinneide.verifiers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.RefactoringCandidatesVerifier;
import br.com.detection.domain.methods.cinneide.Cinneide2000Candidate;
import br.com.detection.domain.methods.cinneide.Cinneide2000FactoryMethodCandidate;
import br.com.detection.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

public abstract class Cinneide2000Verifier implements RefactoringCandidatesVerifier {

	protected final AstHandler astHandler = new AstHandler();

	public List<RefactoringCandidate> retrieveCandidatesFrom(Reference reference, DataHandler dataHandler) {
		final List<Cinneide2000Candidate> candidates = new ArrayList<>();
		final HashSet<String> uniqueCandidates = new HashSet<>();
		final HashSet<Cinneide2000Candidate> realCandidates = new HashSet<>();

		for (Path file : dataHandler.getFiles()) {
			final CompilationUnit parsedClazz = (CompilationUnit) dataHandler.parseFile(file);

			if (parsedClazz == null) {
				continue;
			}

			final Optional<ClassOrInterfaceDeclaration> classOrInterface = this.astHandler
					.getClassOrInterfaceDeclaration(parsedClazz);

			if (!classOrInterface.isPresent() || classOrInterface.get().isInterface()) {
				continue;
			}
			for (MethodDeclaration method : this.astHandler.getMethods(parsedClazz)) {
				List<Optional<Cinneide2000Candidate>> candidate = this.retrieveCandidate(reference, dataHandler, file,
						parsedClazz, classOrInterface.get(), method);
				candidate.forEach(c -> c.ifPresent(candidates::add));
			}
		}

		if (!candidates.isEmpty()) {
			try {
				if (candidates.get(0) instanceof Cinneide2000SingletonCanditate) {
					System.out.println("Singleton Candidate");
					for (Cinneide2000Candidate cinneide2000Candidate : candidates) {
						if (!uniqueCandidates.add(cinneide2000Candidate.getClassName())) {
							realCandidates.add(cinneide2000Candidate);
						}
					}

					candidates.removeAll(realCandidates);

					for (int i = 0; i < candidates.size(); i++) {
						for (Cinneide2000Candidate cd : realCandidates) {
							if (candidates.get(i).getClassName().equals(cd.getClassName())) {
								candidates.remove(i);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				candidates.clear();
			}
		}

		return candidates.stream().map(RefactoringCandidate.class::cast).collect(Collectors.toList());
	}

	private boolean isMethodInvalid(MethodDeclaration method) {
		return method.getParameters() == null || method.getParameters().size() == 0 || method.getParameters().size() > 1
				|| (method.getType() instanceof VoidType);
	}

	private List<Optional<Cinneide2000Candidate>> retrieveCandidate(Reference reference, DataHandler dataHandler,
			Path file, CompilationUnit parsedClazz, ClassOrInterfaceDeclaration classOrInterface,
			MethodDeclaration method) {
		List<Optional<Cinneide2000Candidate>> candidates = new ArrayList<>();
		try {
			if (!this.instancesAreValid(dataHandler, parsedClazz, classOrInterface)) {
				return candidates;
			}

			try {
				List<CompilationUnit> cus = this.validInstances(dataHandler, parsedClazz, classOrInterface);
				for (CompilationUnit cu : cus) {
					Optional<ClassOrInterfaceDeclaration> parsedNewClazz = this.astHandler
							.getClassOrInterfaceDeclaration(cu);
					Path newFile = dataHandler.getFile(cu);
					PackageDeclaration pkgDcl = this.astHandler.getPackageDeclaration(cu);
					candidates.add(Optional
							.of(this.createCandidate(reference, newFile, cu, pkgDcl, parsedNewClazz.get(), method)));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return candidates;
	}

	protected abstract List<CompilationUnit> validInstances(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface);

	protected abstract Cinneide2000Candidate createCandidate(Reference reference, Path file,
			CompilationUnit parsedClazz, PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface,
			MethodDeclaration method);

	protected abstract boolean instancesAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface);

}
