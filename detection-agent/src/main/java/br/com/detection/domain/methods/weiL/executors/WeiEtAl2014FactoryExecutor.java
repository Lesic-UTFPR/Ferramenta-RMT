package br.com.detection.domain.methods.weiL.executors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;

public class WeiEtAl2014FactoryExecutor implements WeiEtAl2014Executor {

	private final AstHandler astHandler = new AstHandler();

	@Override
	public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {
		final WeiEtAl2014FactoryCanditate weiCandidate = (WeiEtAl2014FactoryCanditate) candidate;

		try {
			for (IfStmt ifStmt : weiCandidate.getIfStatements()) {
				this.changesIfStmtCandidate(weiCandidate, ifStmt, dataHandler);
			}

			this.changeBaseClazz(weiCandidate, dataHandler);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void changesIfStmtCandidate(WeiEtAl2014FactoryCanditate candidate, IfStmt ifStmt, DataHandler dataHandler)
			throws IOException {
		final Collection<CompilationUnit> allClasses = this.getParsedClasses(dataHandler);
		final CompilationUnit baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
		final Path file = dataHandler.getFile(baseCu);
		final ClassOrInterfaceDeclaration classDclr = this.astHandler.getClassOrInterfaceDeclaration(baseCu)
				.orElseThrow(IllegalStateException::new);

		final String createdClassName = this.getClassReturnTypeName(classDclr, ifStmt, candidate.getMethodReturnType(),
				dataHandler);
		final String newFactoryClassName = String.format("%sFactory", createdClassName);

		final CompilationUnit cu = new CompilationUnit();
		cu.setPackageDeclaration(baseCu.getPackageDeclaration().orElse(null));
		ClassOrInterfaceDeclaration type = cu.addClass(newFactoryClassName);

		final MethodDeclaration method = new MethodDeclaration();
		method.setName(candidate.getMethodDcl().getName());
		method.setType(candidate.getMethodDcl().getType());
		method.setModifiers(EnumSet.of(Modifier.PUBLIC));
		method.setBody((BlockStmt) ifStmt.getThenStmt());
		
		type.addMember(method);
		type.addExtendedType(classDclr.getNameAsString());

		writeCanges(cu, file.getParent().resolve(String.format("%s.java", newFactoryClassName)));
	}

	private String getClassReturnTypeName(ClassOrInterfaceDeclaration classDclr, IfStmt ifStmt,
			ClassOrInterfaceType returnType, DataHandler dataHandler) {
		final ReturnStmt returnStmt = this.astHandler.getReturnStmt(ifStmt).orElseThrow(IllegalStateException::new);

		final Optional<Node> node = returnStmt.getChildNodes().stream().findFirst();

		if (node.filter(NameExpr.class::isInstance).isPresent()) {

			final String returnName = node.map(NameExpr.class::cast).get().getNameAsString();

			final Optional<VariableDeclarator> varDclr = this.astHandler
					.getVariableDeclarationInNode(ifStmt.getThenStmt(), returnName);

			final ObjectCreationExpr objectCreationExpr = varDclr.map(this.astHandler::getObjectCreationExpr)
					.filter(Optional::isPresent).map(Optional::get).orElseThrow(IllegalStateException::new);

			return this.getClassDeclarationName(objectCreationExpr, dataHandler);
		} else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {

			final ObjectCreationExpr objCreationExpr = node.map(ObjectCreationExpr.class::cast)
					.orElseThrow(IllegalStateException::new);

			return this.getClassDeclarationName(objCreationExpr, dataHandler);
		} else {
			throw new IllegalStateException();
		}
	}

	private String getClassDeclarationName(ObjectCreationExpr objectCreationExpr, DataHandler dataHandler) {

		final Optional<CompilationUnit> cu = Optional
				.ofNullable(dataHandler.getParsedFileByName(objectCreationExpr.getType().getNameAsString()))
				.map(o -> (CompilationUnit) o);

		final Optional<ClassOrInterfaceDeclaration> declaration = cu
				.map(c -> this.astHandler.getClassOrInterfaceDeclaration(c).orElse(null));

		final String className = declaration.map(ClassOrInterfaceDeclaration::getNameAsString)
				.orElseThrow(IllegalStateException::new);

		return className;
	}

	private void changeBaseClazz(WeiEtAl2014Canditate candidate, DataHandler dataHandler) {
		final Collection<CompilationUnit> allClasses = this.getParsedClasses(dataHandler);
		final CompilationUnit baseCu = this.updateBaseCompilationUnit(allClasses, candidate);
		final ClassOrInterfaceDeclaration classDclr = this.astHandler.getClassOrInterfaceDeclaration(baseCu)
				.orElseThrow(IllegalStateException::new);

		final MethodDeclaration candidateMethod = this.astHandler.getMethods(baseCu).stream()
				.filter(m -> m.getNameAsString().equals(candidate.getMethodDcl().getNameAsString())
						&& this.astHandler.methodsParamsMatch(m, candidate.getMethodDcl()))
				.findFirst().orElseThrow(IllegalStateException::new);

		classDclr.setAbstract(true);
		candidateMethod.setBody(null);
		candidateMethod.setAbstract(true);
		candidateMethod.getParameter(0).remove();

		final Path file = dataHandler.getFile(baseCu);

		writeCanges(baseCu, file);
	}

	private CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> allClasses,
			WeiEtAl2014Canditate candidate) {
		return allClasses.stream().filter(c -> {
			return this.astHandler.unitsMatch(c, Optional.of(candidate.getClassDeclaration()),
					Optional.of(candidate.getPackageDeclaration()));
		}).findFirst().get();
	}

	@Override
	public boolean isApplicable(RefactoringCandidate candidate) {
		return candidate instanceof WeiEtAl2014FactoryCanditate
				&& DesignPattern.FACTORY_METHOD.equals(candidate.getEligiblePattern());
	}

}
