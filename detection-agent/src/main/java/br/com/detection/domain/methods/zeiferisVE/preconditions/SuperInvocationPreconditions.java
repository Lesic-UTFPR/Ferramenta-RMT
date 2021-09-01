package br.com.detection.domain.methods.zeiferisVE.preconditions;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;

public class SuperInvocationPreconditions {

	private Set<String> invalidMethodNames = Stream
			.of("toString", "equals", "hashCode", "clone", "finalize", "compareTo").collect(Collectors.toSet());

	private final AstHandler astParser = new AstHandler();

	public SuperInvocationPreconditions() {
	}

	public boolean violatesAmountOfSuperCallsOrName(MethodDeclaration method, Collection<SuperExpr> superCalls) {

		final String name = this.astParser.getSimpleName(method).map(SimpleName::asString).orElse("");

		return superCalls.isEmpty() || superCalls.size() > 1
				|| invalidMethodNames.contains(name) && (name.startsWith("get") || name.startsWith("set"));
	}

	public boolean isOverriddenMethodValid(MethodDeclaration overridenMethod, MethodDeclaration method) {

		final Optional<BlockStmt> blk = overridenMethod.getChildNodes().stream().filter(BlockStmt.class::isInstance)
				.map(BlockStmt.class::cast).findFirst();

		return blk.isPresent() && blk.get().getChildNodes().size() > 1
				&& this.isOverridenMethodLessAccesible(overridenMethod, method);
	}

	private boolean isOverridenMethodLessAccesible(MethodDeclaration overridenMethod, MethodDeclaration method) {
		if (overridenMethod.getModifiers().contains(Modifier.PUBLIC)) {
			return true;
		} else if (overridenMethod.getModifiers().contains(Modifier.PROTECTED)) {
			return method.getModifiers().stream()
					.anyMatch(m -> m.equals(Modifier.PROTECTED) || m.equals(Modifier.PRIVATE));
		} else if (overridenMethod.getModifiers().contains(Modifier.PRIVATE)) {
			return method.getModifiers().stream().anyMatch(m -> m.equals(Modifier.PRIVATE));
		}
		throw new IllegalStateException();
	}

}
