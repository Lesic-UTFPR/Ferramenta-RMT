package br.com.detection.domain.methods.cinneide.verifiers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.detection.domain.methods.cinneide.Cinneide2000Candidate;
import br.com.detection.domain.methods.cinneide.Cinneide2000FactoryMethodCandidate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;

public class Cinneide2000FactoryMethodVerifier extends Cinneide2000Verifier {

	@Override
	protected List<CompilationUnit> validInstances(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface) {

		final List<CompilationUnit> candidates = new ArrayList<>();
		final HashSet<CompilationUnit> finalCandidates = new HashSet<>();
		List<FieldDeclaration> fields = parsedClazz.findAll(FieldDeclaration.class);
		for (FieldDeclaration f : fields) {
			if (f.isPublic()) {
				return candidates;
			}
		}

		for (FieldDeclaration fieldDeclaration : fields) {
			CompilationUnit fieldCu = (CompilationUnit) dataHandler
					.getParsedFileByName(fieldDeclaration.getElementType().toString());
			if (fieldCu != null)
				candidates.add(fieldCu);
			try {
				List<MethodDeclaration> methodsProduct = fieldCu.findAll(MethodDeclaration.class);
				for (MethodDeclaration methodProduct : methodsProduct) {
					if (methodProduct.isStatic()) {
						candidates.clear();
						return candidates;
					}
				}
			} catch (NullPointerException e) {
			}

		}

		Boolean verifier = false;
		try {
			for (CompilationUnit candidate : candidates) {
				try {
					List<FieldDeclaration> fieldsProduct = candidate.findAll(FieldDeclaration.class);
					try {
						for (FieldDeclaration fieldProduct : fieldsProduct) {
							if (fieldProduct.isPublic()) {
								verifier = true;
							}
						}
						if (!verifier) {
							finalCandidates.add(candidate);
						}
						verifier = false;
					} catch (NullPointerException e) {
						continue;
					}

				} catch (NullPointerException e) {
					finalCandidates.add(candidate);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		final List<CompilationUnit> rt = new ArrayList<>(finalCandidates);

		return rt;
	}

	@Override
	protected Cinneide2000Candidate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
			PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method) {
		final ClassOrInterfaceType methodReturnType = this.astHandler.getReturnTypeClassOrInterfaceDeclaration(method)
				.orElse(null);

		return new Cinneide2000FactoryMethodCandidate(reference, file, parsedClazz, pkgDcl, classOrInterface, method,
				methodReturnType);
	}

	@Override
	protected boolean instancesAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface) {
		if (parsedClazz.findAll(ObjectCreationExpr.class).size() > 0) {
			return true;
		}

		return false;
	}

}
