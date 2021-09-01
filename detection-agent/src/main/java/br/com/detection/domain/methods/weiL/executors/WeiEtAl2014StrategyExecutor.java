package br.com.detection.domain.methods.weiL.executors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.TypeParameter;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.weiL.LiteralValueExtractor;
import br.com.detection.domain.methods.weiL.WeiEtAl2014Canditate;
import br.com.detection.domain.methods.weiL.WeiEtAl2014StrategyCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;

public class WeiEtAl2014StrategyExecutor implements WeiEtAl2014Executor {

	private final AstHandler astHandler = new AstHandler();

	@Override
	public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {
		final WeiEtAl2014StrategyCanditate weiCandidate = (WeiEtAl2014StrategyCanditate) candidate;

		try {

			final Path file = dataHandler.getFile(weiCandidate.getCompilationUnit());

			final MethodDeclaration method = new MethodDeclaration();
			method.setName(weiCandidate.getMethodDcl().getName());
			method.setType(weiCandidate.getMethodDcl().getType());
			method.setModifiers(EnumSet.of(Modifier.PUBLIC));
			method.setAbstract(true);

			weiCandidate.getVariables().stream().forEach(v -> method.addParameter(v.getType(), v.getNameAsString()));

			final CompilationUnit strategyCu = new CompilationUnit();
			final ClassOrInterfaceDeclaration createdStrategy = strategyCu.addClass("Strategy");
			createdStrategy.addMember(method);
			createdStrategy.setAbstract(true);

			final Path strategyFile = file.getParent().resolve("Strategy.java");

			writeCanges(strategyCu, strategyFile);

			int i=1;
			for (IfStmt ifStmt : weiCandidate.getIfStatements()) {
				this.changesIfStmtCandidate(i++, file, weiCandidate, ifStmt, dataHandler);
			}

			changeBaseClazz(weiCandidate, createdStrategy, dataHandler);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	private void changesIfStmtCandidate(int idx, Path file, WeiEtAl2014StrategyCanditate candidate, IfStmt ifStmt,
			DataHandler dataHandler) throws IOException {

		final String concreteStrategyClassName = "ConcreteStrategy"
				.concat(this.getNameSuffix(idx, candidate.getMethodDcl(), ifStmt));

		CompilationUnit cu = new CompilationUnit();
		ClassOrInterfaceDeclaration type = cu.addClass(concreteStrategyClassName);

		MethodDeclaration method = new MethodDeclaration();
		method.setName(candidate.getMethodDcl().getName());
		method.setType(candidate.getMethodDcl().getType());
		method.setModifiers(EnumSet.of(Modifier.PUBLIC));
		method.setBody((BlockStmt) ifStmt.getThenStmt());
		candidate.getVariables().forEach(v -> method.addParameter(v.getType(), v.getNameAsString()));

		type.addMember(method);
		type.addExtendedType("Strategy");

		writeCanges(cu, file.getParent().resolve(String.format("%s.java", concreteStrategyClassName)));
	}

	private String getNameSuffix(int idx, MethodDeclaration method, IfStmt ifStmt) {

		final Parameter parameter = method.getParameters().stream().findFirst().get();

		final Optional<BinaryExpr> binaryExpr = ifStmt.getChildNodes().stream().filter(BinaryExpr.class::isInstance)
				.map(BinaryExpr.class::cast).findFirst();

		final Optional<MethodCallExpr> methodCallExpr = ifStmt.getChildNodes().stream()
				.filter(MethodCallExpr.class::isInstance).map(MethodCallExpr.class::cast).findFirst();

		if (binaryExpr.isPresent()) {
			return new LiteralValueExtractor().getNodeOtherThan(binaryExpr.get(), parameter).map(Object::toString).orElse(String.valueOf(idx));
		} else if (methodCallExpr.isPresent()) {
			return new LiteralValueExtractor().getNodeOtherThan(methodCallExpr.get(), parameter).map(Object::toString).orElse(String.valueOf(idx));
		}

		throw new NotImplementedException("Conditional not covered yet");

	}

	private void changeBaseClazz(WeiEtAl2014StrategyCanditate candidate, ClassOrInterfaceDeclaration createdStrategy,
			DataHandler dataHandler) {
		final Collection<CompilationUnit> allClasses = this.getParsedClasses(dataHandler);
		final CompilationUnit baseCu = this.updateBaseCompilationUnit(allClasses, candidate);

		final MethodDeclaration candidateMethod = this.astHandler.getMethods(baseCu).stream()
				.filter(m -> m.getNameAsString().equals(candidate.getMethodDcl().getNameAsString())
						&& this.astHandler.methodsParamsMatch(m, candidate.getMethodDcl()))
				.findFirst().orElseThrow(IllegalStateException::new);

		final MethodCallExpr methodCall = new MethodCallExpr();
		methodCall.setName(candidateMethod.getNameAsString());
		methodCall.setScope(new NameExpr("strategy"));
		methodCall.setArguments(new NodeList<>(
				candidate.getVariables().stream().map(this::parseVariableToFieldAccess).collect(Collectors.toList())));

		final ReturnStmt returnStmt = new ReturnStmt();
		returnStmt.setExpression(methodCall);

		final BlockStmt block = new BlockStmt();
		block.addStatement(returnStmt);
		candidateMethod.setBody(block);
		candidateMethod.setParameter(0,
				new Parameter(new TypeParameter(createdStrategy.getNameAsString()), "strategy"));

		final Path file = dataHandler.getFile(baseCu);

		writeCanges(baseCu, file);
	}

	private FieldAccessExpr parseVariableToFieldAccess(VariableDeclarator var) {
		final FieldAccessExpr fieldAccess = new FieldAccessExpr();
		fieldAccess.setScope(new ThisExpr());
		fieldAccess.setName(new SimpleName(var.getNameAsString()));
		return fieldAccess;
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
		return candidate instanceof WeiEtAl2014StrategyCanditate
				&& DesignPattern.STRATEGY.equals(candidate.getEligiblePattern());
	}

}
