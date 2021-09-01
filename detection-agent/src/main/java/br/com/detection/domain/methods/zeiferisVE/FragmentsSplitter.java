package br.com.detection.domain.methods.zeiferisVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.dataExtractions.ast.utils.NodeConverter;
import io.bretty.console.tree.TreePrinter;

public class FragmentsSplitter {

	private final List<Node> beforeFragment = new ArrayList<>();

	private Node node = null;

	private final List<Node> afterFragment = new ArrayList<>();

	private final AstHandler astHandler = new AstHandler();

	public FragmentsSplitter(MethodDeclaration m, SuperExpr superCall) {
		final Optional<BlockStmt> blockStmt = astHandler.getBlockStatement(m);

		boolean superWasFound = false;
		for (Node child : blockStmt.get().getChildNodes()) {

			if (this.astHandler.childHasDirectSuperCall(child, superCall)) {
				superWasFound = true;
				this.node = child;
				continue;
			}

			this.addToFragment(superWasFound, child);
		}
		
		if(this.node == null) {
			System.out.println("nulo");
		}
	}

	public FragmentsSplitter(MethodDeclaration m, MethodCallExpr methodCall) {
		final Optional<BlockStmt> blockStmt = astHandler.getBlockStatement(m);

		boolean methodCallWasFound = false;
		for (Node child : blockStmt.get().getChildNodes()) {

			if (this.astHandler.nodeHasSameMethodCall(child, methodCall)) {
				methodCallWasFound = true;
				this.node = child;
				continue;
			}

			this.addToFragment(methodCallWasFound, child);
		}
		
		if(this.node == null) {
			System.out.println("nulo");
		}
	}

	private void addToFragment(boolean superWasFound, Node node) {
		if (superWasFound) {
			this.afterFragment.add(node);
		} else {
			this.beforeFragment.add(node);
		}
	}

	public List<Node> getBeforeFragment() {
		return beforeFragment;
	}

	public List<Node> getAfterFragment() {
		return afterFragment;
	}

	public Node getSpecificNode() {
		return node;
	}

	public boolean hasSpecificNode() {
		return this.node != null;
	}

	public Collection<VariableDeclarationExpr> getBeforeVariablesUsedInSpecificNodeAndBeforeFragments() {
		final Collection<VariableDeclarationExpr> variables = this.getBeforeFragment().stream()
				.flatMap(n -> this.astHandler.extractVariableDclrFromNode(n).stream()).collect(Collectors.toList());

		final Optional<MethodCallExpr> methodCall = this.astHandler.getMethodCallExpr(node).stream().findFirst();

		if(!methodCall.isPresent()) {
			
			System.out.println(String.format("Method call not found - %s", NodeConverter.toString(node)));
			
			return Collections.emptyList();
		}
		
		final List<VariableDeclarationExpr> referencedVariables = new ArrayList<>();
		for (VariableDeclarationExpr var : variables) {
			if (this.astHandler.variableIsPresentInMethodCall(var, methodCall.get())) {
				referencedVariables.add(var);
				continue;
			}

			if (this.afterFragmentContaisVariable(var)) {
				referencedVariables.add(var);
			}
		}
		return referencedVariables;
	}

	public Optional<SuperReturnVar> getSuperReturnVariable() {
		if(this.node.getChildNodes() == null || this.node.getChildNodes().isEmpty()) {
			return Optional.empty();
		}
		
		if (this.node.getChildNodes().get(0) instanceof VariableDeclarationExpr) {
			return Optional.of(new SuperReturnVar((VariableDeclarationExpr) this.node.getChildNodes().get(0)));
		} else if (this.node.getChildNodes().get(0) instanceof AssignExpr) {
			final AssignExpr assignment = (AssignExpr) this.node.getChildNodes().get(0);
			return Optional.of(new SuperReturnVar(assignment));
		}
		return Optional.empty();
	}

	private boolean afterFragmentContaisVariable(VariableDeclarationExpr var) {
		return this.getAfterFragment().stream()
				.filter(n -> this.astHandler.nodeHasSimpleName(this.astHandler.getVariableName(var), n)).count() > 0;
	}
	
	private Type getTypeOfVar(NameExpr nameExpr) {
		
		final Collection<VariableDeclarationExpr> declarations = Stream.of(
					this.beforeFragment.stream(),
					Stream.of(this.node),
					this.afterFragment.stream()
				).flatMap(s -> s).map(this.astHandler::extractVariableDclrFromNode).flatMap(Collection::stream).collect(Collectors.toList());
		
		return declarations.stream().flatMap(varDclr -> varDclr.getVariables().stream()).filter(v -> v.getNameAsString().equals(nameExpr.getNameAsString()))
			.findFirst().get().getType();
	}

	public class SuperReturnVar {

		private final Type type;

		private final SimpleName name;

		public SuperReturnVar(VariableDeclarationExpr varDclrExpr) {
			this.type = varDclrExpr.getVariable(0).getType();
			this.name = varDclrExpr.getVariable(0).getName();
		}

		public SuperReturnVar(AssignExpr assignExpr) {
			final NameExpr nameExpr = assignExpr.getChildNodes().stream().filter(NameExpr.class::isInstance).map(NameExpr.class::cast)
					.findFirst().get();
			
			this.type = getTypeOfVar(nameExpr);
			this.name = nameExpr.getName();
		}

		public Type getType() {
			return type;
		}

		public SimpleName getName() {
			return name;
		}

	}

}
