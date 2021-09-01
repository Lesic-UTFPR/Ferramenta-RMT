package br.com.detection.domain.methods.weiL.verifiers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.IfStmt;

import br.com.detection.domain.methods.weiL.LiteralValueExtractor;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.domain.methods.weiL.WeiEtAl2014StrategyCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.detectors.methods.Reference;

public class WeiEtAl2014StrategyVerifier extends WeiEtAl2014Verifier {

	protected boolean ifStmtsAreValid(DataHandler dataHandler, CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method, Collection<IfStmt> ifStatements) {
		return !ifStatements.isEmpty()
				&& ifStatements.stream().allMatch(s -> ifStmtIsValid(parsedClazz, classOrInterface, method, s));
	}

	private boolean ifStmtIsValid(CompilationUnit parsedClazz, ClassOrInterfaceDeclaration classOrInterface,
			MethodDeclaration method, IfStmt ifStmt) {

		final Parameter parameter = method.getParameters().stream().findFirst().get();

		if (!this.astHandler.getReturnStmt(ifStmt).isPresent()) {
			return false;
		}

		final Optional<BinaryExpr> binaryExpr = ifStmt.getChildNodes().stream().filter(BinaryExpr.class::isInstance)
				.map(BinaryExpr.class::cast).findFirst();

		final Optional<MethodCallExpr> methodCallExpr = ifStmt.getChildNodes().stream()
				.filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst();

		final Collection<VariableDeclarator> variables = this.classVariablesUsedInItsBody(parsedClazz, classOrInterface,
				ifStmt);

		return (binaryExpr.isPresent() || methodCallExpr.isPresent())
				&& this.isParameterUsedInIfStmtConditional(parameter, binaryExpr, methodCallExpr)
				&& variables.size() > 0 && variables.size() < 2 && this.usesNoMethodInnerVariables(method, ifStmt);
	}

	private boolean usesNoMethodInnerVariables(MethodDeclaration method, IfStmt ifStmt) {

		final Collection<VariableDeclarator> variables = method.getChildNodes().stream()
				.filter(c -> !(c instanceof IfStmt)).flatMap(c -> this.astHandler.getVariableDeclarations(c).stream())
				.collect(Collectors.toList());

		return variables.stream().filter(v -> {
			return ifStmt.getThenStmt().getChildNodes().stream().filter(c -> this.astHandler.nodeUsesVar(c, v))
					.findFirst().isPresent();
		}).count() == 0;
	}

	private Collection<VariableDeclarator> classVariablesUsedInItsBody(CompilationUnit parsedClazz,
			ClassOrInterfaceDeclaration classOrInterface, IfStmt ifStmt) {

		final Collection<FieldDeclaration> fields = this.astHandler.getDeclaredFields(classOrInterface);

		return fields.stream().flatMap(f -> f.getVariables().stream())
				.filter(var -> this.astHandler.nodeUsesVar(ifStmt, var)).collect(Collectors.toList());
	}

	private boolean isParameterUsedInIfStmtConditional(Parameter parameter, Optional<BinaryExpr> binaryExpr,
			Optional<MethodCallExpr> methodCallExpr) {

		if (binaryExpr.isPresent()) {
			final String name2 = this.astHandler.getNameExpr(binaryExpr.get()).map(n -> n.getNameAsString()).orElse("");

			return parameter.getNameAsString().equals(name2) && isAnEqualsExpression(binaryExpr.get());
		} else if (methodCallExpr.isPresent()) {

			final boolean hasParam = methodCallExpr.get().getChildNodes().stream().filter(NameExpr.class::isInstance)
					.map(NameExpr.class::cast).filter(n -> {
						return n.getNameAsString().equals(parameter.getNameAsString());
					}).findFirst().isPresent();

			final boolean isAnEqualsMethod = methodCallExpr.get().getNameAsString().equals("equals")
					&& methodCallExpr.get().getChildNodes().stream().filter(NameExpr.class::isInstance)
							.map(NameExpr.class::cast).filter(n -> {
								return n.getNameAsString().equals(parameter.getNameAsString());
							}).findFirst().isPresent();

			return hasParam && isAnEqualsMethod;
		}

		return false;
	}
	

	private boolean isAnEqualsExpression(BinaryExpr binaryExpr) {
		return binaryExpr.getOperator().equals(BinaryExpr.Operator.EQUALS);
	}

	@Override
	protected WeiEtAl2014Canditate createCandidate(Reference reference, Path file, CompilationUnit parsedClazz,
			PackageDeclaration pkgDcl, ClassOrInterfaceDeclaration classOrInterface, MethodDeclaration method,
			Collection<IfStmt> ifStatements) {

		final List<VariableDeclarator> variables = new ArrayList<>();

		final Function<VariableDeclarator, Boolean> isVariableRegistered = (var) -> variables.stream()
				.anyMatch(v -> v.getNameAsString().equals(var.getNameAsString()));

		ifStatements.stream().flatMap(s -> this.classVariablesUsedInItsBody(parsedClazz, classOrInterface, s).stream())
				.forEach(v -> {
					if (!isVariableRegistered.apply(v)) {
						variables.add(v);
					}
				});

		return new WeiEtAl2014StrategyCanditate(reference, file, parsedClazz, pkgDcl, classOrInterface, method,
				ifStatements, variables);
	}

}
