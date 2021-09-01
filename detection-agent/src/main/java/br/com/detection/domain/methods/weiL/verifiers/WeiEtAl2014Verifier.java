package br.com.detection.domain.methods.weiL.verifiers;

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
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.VoidType;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.RefactoringCandidatesVerifier;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

public abstract class WeiEtAl2014Verifier implements RefactoringCandidatesVerifier {

	protected final AstHandler astHandler = new AstHandler();

	public List<RefactoringCandidate> retrieveCandidatesFrom(Reference reference, DataHandler dataHandler) {
		final List<WeiEtAl2014Canditate> candidates = new ArrayList<>();

		for (Path file : dataHandler.getFiles()) {
			final CompilationUnit parsedClazz = (CompilationUnit) dataHandler.parseFile(file);
			
			if(parsedClazz == null) {
				continue;
			}

			final Optional<ClassOrInterfaceDeclaration> classOrInterface = this.astHandler
					.getClassOrInterfaceDeclaration(parsedClazz);

			if (!classOrInterface.isPresent() || classOrInterface.get().isInterface()) {
				continue;
			}

			for (MethodDeclaration method : this.astHandler.getMethods(parsedClazz)) {

				final Optional<WeiEtAl2014Canditate> candidate = this.retrieveCandidate(reference, dataHandler, file, parsedClazz,
						classOrInterface.get(), method);

				candidate.ifPresent(candidates::add);
			}
		}
		return candidates.stream().map(RefactoringCandidate.class::cast).collect(Collectors.toList());
	}

	private boolean isMethodInvalid(MethodDeclaration method) {
		return method.getParameters() == null || method.getParameters().size() == 0 || method.getParameters().size() > 1
				|| (method.getType() instanceof VoidType);
	}

	private Optional<WeiEtAl2014Canditate> retrieveCandidate(Reference reference, DataHandler dataHandler, Path file,
			CompilationUnit parsedClazz, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method) {

		if (this.isMethodInvalid(method)) {
			return Optional.empty();
		}

		final Collection<IfStmt> ifStatements = this.astHandler.getIfStatements(method);

		if (!this.ifStmtsAreValid(dataHandler, parsedClazz, classOrInterface, method, ifStatements)) {
			return Optional.empty();
		}

		final PackageDeclaration pkgDcl = this.astHandler.getPackageDeclaration(parsedClazz);

		return Optional
				.of(this.createCandidate(reference, file, parsedClazz, pkgDcl, classOrInterface, method, ifStatements));
	}

	protected abstract WeiEtAl2014Canditate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
			PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method,
			Collection<IfStmt> ifStatements);

	protected abstract boolean ifStmtsAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method, Collection<IfStmt> ifStatements);

}
