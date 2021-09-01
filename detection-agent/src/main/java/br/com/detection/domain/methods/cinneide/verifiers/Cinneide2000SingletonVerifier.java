package br.com.detection.domain.methods.cinneide.verifiers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import br.com.detection.domain.methods.cinneide.Cinneide2000Candidate;
import br.com.detection.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;

public class Cinneide2000SingletonVerifier extends Cinneide2000Verifier {

	@Override
	protected Cinneide2000Candidate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
			PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method) {

		final ClassOrInterfaceType methodReturnType = this.astHandler.getReturnTypeClassOrInterfaceDeclaration(method)
				.orElse(null);

		return new Cinneide2000SingletonCanditate(reference, file, parsedClazz, pkgDcl, classOrInterface, method,
				methodReturnType);
	}

	@Override
	protected boolean instancesAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface) {
		final Collection<ObjectCreationExpr> uniqueInstance = getUnique(getInstance(parsedClazz, dataHandler));
		if (uniqueInstance.isEmpty() || !hasOneConstructor(parsedClazz))
			return false;

		return true;
	}

	public static boolean hasOneConstructor(CompilationUnit cu) {
		if (cu.findAll(ConstructorDeclaration.class).size() > 1)
			return false;
		return true;
	}

	public static Collection<ObjectCreationExpr> getUnique(Collection<ObjectCreationExpr> instances) {
		try {
			HashSet<Type> FieldSet = new HashSet<>();
			List<ObjectCreationExpr> te = new ArrayList<>();
			for (ObjectCreationExpr instance : instances) {
				if (!FieldSet.add(instance.getType())) {
					te.add(instance);
				}
			}
			instances.removeAll(te);
		} catch (Exception e) {
			instances.clear();
		}
		return instances;
	}

	public static Collection<ObjectCreationExpr> getInstance(CompilationUnit cu, DataHandler dataHandler) {
		List<ObjectCreationExpr> instance = new ArrayList<>();

		try {
			cu.findAll(ObjectCreationExpr.class).stream().forEach(i -> {
				Type element = i.getType();
				if (element.isReferenceType()) {
					if (dataHandler.getParsedFileByName(i.getType().toString()) != null
							&& !i.getType().toString().contains("Exception")) {
						instance.add(i);
					}
				}
			});
		} catch (Exception e) {
			instance.clear();
		}
		return instance;
	}

	@Override
	protected List<CompilationUnit> validInstances(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface) {
		final List<ObjectCreationExpr> uniqueInstance = (List<ObjectCreationExpr>) getUnique(
				getInstance(parsedClazz, dataHandler));
		final List<CompilationUnit> clazzes = new ArrayList<>();
		try {
			for (ObjectCreationExpr ui : uniqueInstance) {
				clazzes.add((CompilationUnit) dataHandler.getParsedFileByName(ui.getTypeAsString()));

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return clazzes;
	}

}
