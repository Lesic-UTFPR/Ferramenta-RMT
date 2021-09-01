package br.com.detection.domain.methods.zeiferisVE.executors;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.zeiferisVE.FragmentsSplitter;
import br.com.detection.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import br.com.detection.domain.methods.zeiferisVE.FragmentsSplitter.SuperReturnVar;
import br.com.detection.methods.dataExtractions.forks.DataHandler;

public class ZafeirisEtAl2016Executor {

	private final AstHandler astHandler = new AstHandler();

	private Collection<CompilationUnit> getParsedClasses(DataHandler dataHandler) {
		return dataHandler.getParsedFiles().stream().map(CompilationUnit.class::cast).collect(Collectors.toList());
	}

	public void refactor(ZafeirisEtAl2016Canditate candidate, DataHandler dataHandler) {

		Collection<CompilationUnit> allClasses = this.getParsedClasses(dataHandler);
		CompilationUnit childCU = candidate.getCompilationUnit();
		CompilationUnit parentCU = this.updateParent(allClasses, childCU, candidate);

		final MethodDeclaration newOverridenMethod = extractMethodOnOverridenMethod(dataHandler, candidate, parentCU);

		allClasses = this.getParsedClasses(dataHandler);
		childCU = this.updateChild(allClasses, candidate);
		parentCU = this.updateParent(allClasses, childCU, candidate);

		final MethodCallExpr newDoOverridenCall = replaceSuperCallByDoOverriden(dataHandler, childCU, parentCU,
				newOverridenMethod);

		allClasses = this.getParsedClasses(dataHandler);
		childCU = this.updateChild(allClasses, candidate);
		parentCU = this.updateParent(allClasses, childCU, candidate);

		extractMethodOnBeforeAndAfterFragments(dataHandler, candidate, childCU, parentCU, newDoOverridenCall);

		allClasses = this.getParsedClasses(dataHandler);
		childCU = this.updateChild(allClasses, candidate);
		parentCU = this.updateParent(allClasses, childCU, candidate);

		pullUpOverridenMethod(dataHandler, candidate, parentCU, childCU);

		allClasses = this.getParsedClasses(dataHandler);
		childCU = this.updateChild(allClasses, candidate);
		parentCU = this.updateParent(allClasses, childCU, candidate);

		applyFinalAdjustments(dataHandler, candidate, parentCU, childCU);
	}

	private CompilationUnit updateChild(Collection<CompilationUnit> allClasses, ZafeirisEtAl2016Canditate candidate) {
		return allClasses.stream().filter(c -> {
			return this.astHandler.unitsMatch(c, Optional.of(candidate.getClassDeclaration()),
					Optional.of(candidate.getPackageDeclaration()));
		}).findFirst().get();
	}

	private CompilationUnit updateParent(Collection<CompilationUnit> allClasses, CompilationUnit childCU,
			ZafeirisEtAl2016Canditate candidate) {
		return this.astHandler.getParent(childCU, allClasses).orElseThrow(() -> new IllegalStateException());
	}

	private void applyFinalAdjustments(DataHandler dataHandler, ZafeirisEtAl2016Canditate candidate,
			CompilationUnit parentCU, CompilationUnit childCU) {

		final MethodDeclaration overridenMethodDclr = this.astHandler.getMethods(parentCU).stream()
				.filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException());

		overridenMethodDclr.setFinal(true);

		writeCanges(parentCU, dataHandler.getFile(parentCU));

	}

	private void pullUpOverridenMethod(DataHandler dataHandler, ZafeirisEtAl2016Canditate candidate,
			CompilationUnit parentCU, CompilationUnit childCU) {

		final MethodDeclaration overridenMethodDclr = this.astHandler.getMethods(parentCU).stream()
				.filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException());

		final MethodDeclaration overridingMethodDclr = this.astHandler.getMethods(childCU).stream()
				.filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridingMethod())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException());

		overridenMethodDclr.setBody(new BlockStmt());

		overridingMethodDclr.getBody()
				.ifPresent(b -> b.getStatements().forEach(overridenMethodDclr.getBody().get()::addStatement));

		final ClassOrInterfaceDeclaration childClass = this.astHandler.getClassOrInterfaceDeclaration(childCU).get();

		childClass.remove(overridingMethodDclr);

		writeCanges(parentCU, dataHandler.getFile(parentCU));
		writeCanges(childCU, dataHandler.getFile(childCU));
	}

	private void extractMethodOnBeforeAndAfterFragments(DataHandler dataHandler, ZafeirisEtAl2016Canditate candidate,
			CompilationUnit childCU, CompilationUnit parentCU, MethodCallExpr newDoOverridenCall) {

		final MethodDeclaration childMethodDclr = this.astHandler.getMethods(childCU).stream()
				.filter(m -> m.getNameAsString().equals(candidate.getOverridenMethod().getNameAsString()))
				.filter(m -> this.astHandler.methodsParamsMatch(m, candidate.getOverridenMethod())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException());

		final FragmentsSplitter fragmentsSplitter = new FragmentsSplitter(childMethodDclr, newDoOverridenCall);

		final Node beforeFragmentReturnValue = this.applyExtractMethodOnBeforeFragment(parentCU, childCU,
				childMethodDclr, fragmentsSplitter);
		final MethodDeclaration afterFragmentMethod = this.applyExtractMethodOnAfterFragment(parentCU, childCU,
				childMethodDclr, fragmentsSplitter, Optional.of(beforeFragmentReturnValue)
						.filter(VariableDeclarationExpr.class::isInstance).map(VariableDeclarationExpr.class::cast));

		applyBeforeAndAfterFragmentsInSourceMethod(fragmentsSplitter, childMethodDclr, beforeFragmentReturnValue,
				newDoOverridenCall, afterFragmentMethod);

		writeCanges(parentCU, dataHandler.getFile(parentCU));
		writeCanges(childCU, dataHandler.getFile(childCU));
	}

	private void applyBeforeAndAfterFragmentsInSourceMethod(FragmentsSplitter fragmentsSplitter,
			MethodDeclaration childMethodDclr, Node beforeFragmentReturnValue, MethodCallExpr newDoOverridenCall,
			MethodDeclaration afterFragmentMethod) {

		VariableDeclarationExpr superCallThroughAssignment = null;
		final Optional<SuperReturnVar> superReturnVar = fragmentsSplitter.getSuperReturnVariable();
		if (superReturnVar.isPresent()) {
			superCallThroughAssignment = new VariableDeclarationExpr(
					new VariableDeclarator(superReturnVar.get().getType(), "superReturnVar", newDoOverridenCall));
		}

		final MethodCallExpr afterFragmentMethodCallExpr = new MethodCallExpr(afterFragmentMethod.getNameAsString());
		childMethodDclr.getParameters().forEach(p -> afterFragmentMethodCallExpr.addArgument(p.getName().asString()));
		if (beforeFragmentReturnValue instanceof VariableDeclarationExpr) {
			afterFragmentMethodCallExpr.addArgument(
					((VariableDeclarationExpr) beforeFragmentReturnValue).getVariable(0).getNameAsString());
		}
		if (superReturnVar.isPresent()) {
			afterFragmentMethodCallExpr.addArgument(superCallThroughAssignment.getVariable(0).getNameAsString());
		}
		final ReturnStmt returnStmt = new ReturnStmt(afterFragmentMethodCallExpr);

		final BlockStmt block = new BlockStmt();
		block.addStatement(Optional.of(beforeFragmentReturnValue).filter(Expression.class::isInstance)
				.map(Expression.class::cast).orElseThrow(IllegalStateException::new));
		block.addStatement(Optional.ofNullable((Expression) superCallThroughAssignment).orElse(newDoOverridenCall));
		block.addStatement(returnStmt);

		childMethodDclr.setBody(block);
	}

	private Node applyExtractMethodOnBeforeFragment(CompilationUnit parentCU, CompilationUnit childCU,
			MethodDeclaration childMethodDclr, FragmentsSplitter fragmentsSplitter) {

		final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(childCU)
				.orElseThrow(() -> new IllegalArgumentException());

		final String beforeMethodName = String.format("before%s%s",
				childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
				childMethodDclr.getName().asString().substring(1));

		childClassDclr.addMethod(beforeMethodName, Modifier.PROTECTED);

		final Collection<MethodDeclaration> methods = this.astHandler.getMethods(childCU);

		final MethodDeclaration newMethodDclr = methods.stream().filter(
				m -> this.astHandler.getSimpleName(m).filter(sn -> sn.asString().equals(beforeMethodName)).isPresent())
				.findFirst().orElseThrow(() -> new IllegalArgumentException());

		final BlockStmt block = new BlockStmt();

		fragmentsSplitter.getBeforeFragment().stream().filter(Statement.class::isInstance).map(Statement.class::cast)
				.forEach(node -> block.addStatement(node));

		newMethodDclr.setBody(block);
		childMethodDclr.getParameters().forEach(param -> newMethodDclr.addParameter(param));
		childMethodDclr.getThrownExceptions().forEach(exp -> newMethodDclr.addThrownException(exp));

		final MethodCallExpr methodCallExpr = new MethodCallExpr(beforeMethodName);
		newMethodDclr.getParameters().stream().map(Parameter::getName).map(NameExpr::new)
				.forEach(methodCallExpr.getArguments()::add);

		final Collection<VariableDeclarationExpr> variables = fragmentsSplitter
				.getBeforeVariablesUsedInSpecificNodeAndBeforeFragments();

		if (variables.size() == 1) {

			final VariableDeclarationExpr varDclrExpr = variables.stream().findFirst().get();

			final VariableDeclarator varDclr = varDclrExpr.getVariables().get(0);

			final ReturnStmt returnStmt = new ReturnStmt(new NameExpr(varDclr.getName().asString()));

			block.addStatement(returnStmt);

			newMethodDclr.setType(varDclr.getType());

			final VariableDeclarationExpr thisMethodCallDclrExpr = new VariableDeclarationExpr(
					new VariableDeclarator(varDclr.getType(), varDclr.getName(), methodCallExpr));

			createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

			return thisMethodCallDclrExpr;
		} else if (variables.size() > 1) {
			throw new IllegalStateException();
		}

		createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

		return methodCallExpr;
	}

	private void createHookMethod(CompilationUnit parentCU, MethodDeclaration newMethodDclr,
			FragmentsSplitter fragmentsSplitter) {

		final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(parentCU)
				.orElseThrow(() -> new IllegalArgumentException());

		childClassDclr.addMethod(newMethodDclr.getNameAsString(), Modifier.PROTECTED);

		final Collection<MethodDeclaration> methods = this.astHandler.getMethods(parentCU);

		final MethodDeclaration hookMethodDclr = methods.stream()
				.filter(m -> this.astHandler.getSimpleName(m)
						.filter(sn -> sn.asString().equals(newMethodDclr.getNameAsString())).isPresent())
				.findFirst().orElseThrow(() -> new IllegalArgumentException());

		newMethodDclr.getParameters()
				.forEach(p -> hookMethodDclr.getParameters().add(new Parameter(p.getType(), p.getName())));
		newMethodDclr.getThrownExceptions().forEach(exp -> hookMethodDclr.addThrownException(exp));

		Optional.ofNullable(newMethodDclr.getType()).ifPresent(hookMethodDclr::setType);

		final Optional<ReturnStmt> returnStmt = newMethodDclr.getBody()
				.filter(b -> !b.getStatements().isEmpty())
				.filter(b -> b.getStatement(b.getStatements().size() - 1) != null)
				.map(b -> b.getStatement(b.getStatements().size() - 1)).filter(ReturnStmt.class::isInstance)
				.map(ReturnStmt.class::cast);

		hookMethodDclr.setBody(new BlockStmt());

		final Collection<VariableDeclarationExpr> variableDeclarationExpressions = fragmentsSplitter
				.getBeforeVariablesUsedInSpecificNodeAndBeforeFragments();

		variableDeclarationExpressions.stream().findFirst().ifPresent(hookMethodDclr.getBody().get()::addStatement);

		if (returnStmt.isPresent()) {
			hookMethodDclr.getBody().get().addStatement(returnStmt.get());
		}
	}

	private MethodDeclaration applyExtractMethodOnAfterFragment(CompilationUnit parentCU, CompilationUnit childCU,
			MethodDeclaration childMethodDclr, FragmentsSplitter fragmentsSplitter,
			Optional<VariableDeclarationExpr> beforeFragmentReturnValue) {

		final ClassOrInterfaceDeclaration childClassDclr = this.astHandler.getClassOrInterfaceDeclaration(childCU)
				.orElseThrow(() -> new IllegalArgumentException());

		final Optional<SuperReturnVar> superReturnVar = fragmentsSplitter.getSuperReturnVariable();

		final String afterMethodName = String.format("after%s%s",
				childMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
				childMethodDclr.getName().asString().substring(1));

		childClassDclr.addMethod(afterMethodName, Modifier.PROTECTED);

		final Collection<MethodDeclaration> methods = this.astHandler.getMethods(childCU);

		final MethodDeclaration newMethodDclr = methods.stream().filter(
				m -> this.astHandler.getSimpleName(m).filter(sn -> sn.asString().equals(afterMethodName)).isPresent())
				.findFirst().orElseThrow(() -> new IllegalArgumentException());

		final BlockStmt block = new BlockStmt();

		fragmentsSplitter.getAfterFragment().stream().filter(Statement.class::isInstance).map(Statement.class::cast)
				.forEach(node -> block.addStatement(node));

		newMethodDclr.setBody(block);
		newMethodDclr.setType(childMethodDclr.getType());
		childMethodDclr.getTypeParameters().forEach(tparam -> newMethodDclr.getTypeParameters().add(tparam));
		childMethodDclr.getParameters().forEach(param -> newMethodDclr.addParameter(param));
		childMethodDclr.getThrownExceptions().forEach(exp -> newMethodDclr.addThrownException(exp));

		if (beforeFragmentReturnValue.isPresent()) {
			final VariableDeclarator varDclr = beforeFragmentReturnValue.get().getVariables().get(0);

			newMethodDclr.addParameter(varDclr.getType(), varDclr.getNameAsString());
		}

		if (superReturnVar.isPresent()) {
			newMethodDclr.addParameter(superReturnVar.get().getType(), superReturnVar.get().getName().asString());
		}

		createHookMethod(parentCU, newMethodDclr, fragmentsSplitter);

		return newMethodDclr;
	}

	private MethodCallExpr replaceSuperCallByDoOverriden(DataHandler dataHandler, CompilationUnit childCU,
			CompilationUnit parentCu, MethodDeclaration newOverridenMethod) {

		final SuperExpr superExpr = this.astHandler.getSuperCalls(childCU).stream().findFirst().get();

		final MethodCallExpr superMethodCall = (MethodCallExpr) superExpr.getParentNode().get();

		final ExpressionStmt node = this.astHandler.getExpressionStatement(superExpr).get();

		final MethodCallExpr newMethodCall = new MethodCallExpr(newOverridenMethod.getNameAsString());

		superMethodCall.getArguments().forEach(a -> newMethodCall.addArgument(a));

		if (node.getChildNodes().get(0) instanceof VariableDeclarationExpr) {

			final VariableDeclarationExpr oldVariableDeclaration = (VariableDeclarationExpr) node.getChildNodes()
					.get(0);

			final VariableDeclarator oldVariableDeclarator = (VariableDeclarator) oldVariableDeclaration.getChildNodes()
					.get(0);

			final VariableDeclarator varibleDeclarator = new VariableDeclarator(oldVariableDeclarator.getType(),
					oldVariableDeclarator.getName(), newMethodCall);

			final VariableDeclarationExpr newVariableDeclaration = new VariableDeclarationExpr(varibleDeclarator);

			node.setExpression(newVariableDeclaration);

		} else if (node.getChildNodes().get(0) instanceof MethodCallExpr) {
			node.setExpression(newMethodCall);
		} else if (node.getChildNodes().get(0) instanceof AssignExpr) {
			final AssignExpr oldAssignment = (AssignExpr) node.getChildNodes().get(0);

			node.setExpression(new AssignExpr(oldAssignment.getTarget(), newMethodCall, Operator.ASSIGN));
		} else {
			throw new UnsupportedOperationException();
		}

		writeCanges(childCU, dataHandler.getFile(childCU));

		return newMethodCall;
	}

	private MethodDeclaration extractMethodOnOverridenMethod(DataHandler dataHandler,
			ZafeirisEtAl2016Canditate candidade, CompilationUnit parentCu) {

		final ClassOrInterfaceDeclaration parentClassDclr = this.astHandler.getClassOrInterfaceDeclaration(parentCu)
				.orElseThrow(() -> new IllegalArgumentException());

		final MethodDeclaration oldMethodDclr = this.astHandler.getMethods(parentCu).stream()
				.filter(m -> this.astHandler.methodsParamsMatch(m, candidade.getOverridenMethod())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException());

		final String newMethodName = String.format("do%s%s",
				oldMethodDclr.getName().asString().substring(0, 1).toUpperCase(),
				oldMethodDclr.getName().asString().substring(1));

		parentClassDclr.addMethod(newMethodName, Modifier.PRIVATE);

		final Collection<MethodDeclaration> methods = this.astHandler.getMethods(parentCu);

		final MethodDeclaration newMethodDclr = methods.stream().filter(
				m -> this.astHandler.getSimpleName(m).filter(sn -> sn.asString().equals(newMethodName)).isPresent())
				.findFirst().orElseThrow(() -> new IllegalArgumentException());

		newMethodDclr.setBody(oldMethodDclr.getBody().orElseThrow(() -> new IllegalArgumentException()));
		newMethodDclr.setType(oldMethodDclr.getType());
		oldMethodDclr.getTypeParameters().forEach(tparam -> newMethodDclr.getTypeParameters().add(tparam));
		oldMethodDclr.getParameters().forEach(param -> newMethodDclr.addParameter(param));
		oldMethodDclr.getThrownExceptions().forEach(exp -> newMethodDclr.addThrownException(exp));

		final MethodCallExpr methodCallExpr = new MethodCallExpr(newMethodName);
		oldMethodDclr.getParameters().forEach(p -> methodCallExpr.addArgument(p.getName().asString()));
		final ReturnStmt returnStmt = new ReturnStmt(methodCallExpr);

		oldMethodDclr.setBody(new BlockStmt());
		oldMethodDclr.getBody().get().addStatement(returnStmt);

		writeCanges(parentCu, dataHandler.getFile(parentCu));

		return newMethodDclr;
	}

	private void writeCanges(CompilationUnit cUnit, Path file) {
		try (FileWriter fileWriter = new FileWriter(file.toFile())) {
			fileWriter.write(cUnit.toString());
			fileWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

}
