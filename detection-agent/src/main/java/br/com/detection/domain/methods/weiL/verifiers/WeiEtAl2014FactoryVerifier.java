package br.com.detection.domain.methods.weiL.verifiers;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.detection.domain.methods.weiL.LiteralValueExtractor;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;

public class WeiEtAl2014FactoryVerifier extends WeiEtAl2014Verifier {

	protected boolean ifStmtsAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method, Collection<IfStmt> ifStatements) {
		final Optional<ClassOrInterfaceType> baseType = this.astHandler
				.getReturnTypeClassOrInterfaceDeclaration(method);

		if (!baseType.isPresent()) {
			return false;
		}

		final Parameter parameter = method.getParameters().stream().findFirst().get();

		return !ifStatements.isEmpty()
				&& ifStatements.stream().allMatch(s -> ifStmtIsValid(dataHandler, baseType.get(), parameter, s));
	}

	private boolean ifStmtIsValid(DataHandler dataHandler, ClassOrInterfaceType baseType, Parameter parameter,
			IfStmt ifStmt) {

		final Optional<BinaryExpr> binaryExpr = ifStmt.getChildNodes().stream().filter(BinaryExpr.class::isInstance)
				.map(BinaryExpr.class::cast).findFirst();

		final Optional<MethodCallExpr> methodCallExpr = ifStmt.getChildNodes().stream()
				.filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst();

		final boolean parameterIsUsed = (binaryExpr.isPresent() || methodCallExpr.isPresent())
				&& isParameterUsedInIfStmtConditional(parameter, binaryExpr, methodCallExpr);

		final boolean hasValidReturn = this.hasReturnTypeAndHasValidSubtype(dataHandler, baseType, ifStmt);

		return parameterIsUsed && hasValidReturn;
	}

	private boolean hasReturnTypeAndHasValidSubtype(DataHandler dataHandler, ClassOrInterfaceType baseType,
			IfStmt ifStmt) {
		final Optional<ReturnStmt> returnStmt = this.astHandler.getReturnStmt(ifStmt);

		if (returnStmt.isPresent()) {
			final Optional<Node> node = returnStmt.get().getChildNodes().stream().findFirst();

			if (node.filter(NameExpr.class::isInstance).isPresent()) {

				final String returnName = node.map(NameExpr.class::cast).get().getNameAsString();

				final Optional<VariableDeclarator> varDclr = this.astHandler.getVariableDeclarationInNode(ifStmt.getThenStmt(),
						returnName);

				final Optional<ObjectCreationExpr> objectCreationExpr = varDclr
						.map(this.astHandler::getObjectCreationExpr).filter(Optional::isPresent).map(Optional::get);

				final boolean typeMatches = this.isOfTypeOrIsSubtype(dataHandler, baseType, objectCreationExpr);
				
				return typeMatches;
			} else if (node.filter(ObjectCreationExpr.class::isInstance).isPresent()) {

				final Optional<ObjectCreationExpr> objCreationExpr = node.map(ObjectCreationExpr.class::cast);

				final boolean typeMatches = this.isOfTypeOrIsSubtype(dataHandler, baseType, objCreationExpr);
				
				return typeMatches;
			}
		}
		return false;
	}

	private boolean isOfTypeOrIsSubtype(DataHandler dataHandler, ClassOrInterfaceType type,
			Optional<ObjectCreationExpr> objCreationExpr) {

		if (!objCreationExpr.isPresent()) {
			return false;
		}

		final Optional<ClassOrInterfaceType> classOrInterfaceType = objCreationExpr.map(ObjectCreationExpr::getType);
		
		if(!classOrInterfaceType.isPresent()) {
			return false;
		}

		final Optional<CompilationUnit> cu = Optional
				.ofNullable(
						dataHandler.getParsedFileByName(classOrInterfaceType.map(d -> d.getNameAsString()).orElse("")))
				.map(o -> (CompilationUnit) o);

		final Optional<ClassOrInterfaceDeclaration> declaration = cu
				.map(c -> this.astHandler.getClassOrInterfaceDeclaration(c).orElse(null));

		return declaration.isPresent() && cu.isPresent()
				&& (declaration.get().getExtendedTypes().stream().filter(t -> t.equals(type)).findFirst().isPresent()
						|| declaration.get().getImplementedTypes().stream().filter(t -> t.equals(type)).findFirst()
								.isPresent());
	}

	private boolean isParameterUsedInIfStmtConditional(Parameter parameter, Optional<BinaryExpr> binaryExpr,
			Optional<MethodCallExpr> methodCallExpr) {

		if (binaryExpr.isPresent()) {
			final String name2 = this.astHandler.getNameExpr(binaryExpr.get()).map(n -> n.getNameAsString()).orElse("");

			return parameter.getNameAsString().equals(name2)
					&& BinaryExpr.Operator.EQUALS.equals(binaryExpr.get().getOperator()) ;
		} else if (methodCallExpr.isPresent()) {

			final boolean hasParam = methodCallExpr.get().getChildNodes().stream().filter(NameExpr.class::isInstance)
					.map(NameExpr.class::cast).filter(n -> {
						return n.getNameAsString().equals(parameter.getNameAsString());
					}).findFirst().isPresent();

			final boolean isAnEqualsMethod = methodCallExpr.get().getChildNodes().stream()
					.filter(SimpleName.class::isInstance).map(SimpleName.class::cast).filter(n -> {
						return n.asString().equals(parameter.getNameAsString());
					}).findFirst().isPresent();

			return hasParam && isAnEqualsMethod;
		}

		return false;
	}

	@Override
	protected WeiEtAl2014Canditate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
			PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method,
			Collection<IfStmt> ifStatements) {

		final ClassOrInterfaceType methodReturnType = this.astHandler.getReturnTypeClassOrInterfaceDeclaration(method)
				.orElse(null);

		return new WeiEtAl2014FactoryCanditate(reference, file, parsedClazz, pkgDcl, classOrInterface, method,
				methodReturnType, ifStatements);
	}

}
