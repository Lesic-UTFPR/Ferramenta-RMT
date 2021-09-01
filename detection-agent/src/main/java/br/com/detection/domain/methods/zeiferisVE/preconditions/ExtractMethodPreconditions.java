package br.com.detection.domain.methods.zeiferisVE.preconditions;

import java.util.Collection;
import java.util.Optional;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.zeiferisVE.FragmentsSplitter;

public class ExtractMethodPreconditions {

	private final AstHandler astHandler = new AstHandler();

	public boolean isValid(MethodDeclaration overrienMethod, MethodDeclaration m, SuperExpr superCall) {
		final FragmentsSplitter fragmentsSplitter = new FragmentsSplitter(m, superCall);

		return fragmentsSplitter.hasSpecificNode() 
				&& this.superCallIsNotNested(m, superCall) && this.beforeFragmentThrowsNoException(fragmentsSplitter)
				&& this.beforeFragmentHasNoReturn(fragmentsSplitter)
				&& !this.afterAndSuperUseMoreThanOneVariableOfBefore(fragmentsSplitter)
				&& this.methodsValuesMatch(overrienMethod, m) && this.fragmentsHaveMinSize(fragmentsSplitter);
	}

	private boolean fragmentsHaveMinSize(FragmentsSplitter fragmentsSplitter) {
		return fragmentsSplitter.getBeforeFragment().size() > 2 || fragmentsSplitter.getAfterFragment().size() > 2;
	}

	private boolean methodsValuesMatch(MethodDeclaration m1, MethodDeclaration m2) {
		
		if(m1.getParameters() == null && m2.getParameters() == null) {
			return true;
		} else if( (m1.getParameters() == null && m2.getParameters() != null) || (m1.getParameters() != null && m2.getParameters() == null) ) {
			return false;
		}
		
		int differentValuesCounter = 0;
		for (int i = 0; i < m1.getParameters().size(); i++) {

			final Object v1 = m1.getParameters().get(i).getData(new DataKey<Object>() {
			});

			final Object v2 = m2.getParameters().get(i).getData(new DataKey<Object>() {
			});

			differentValuesCounter += v1 == null && v2 == null ? 0 : (v1.equals(v2) ? 0 : 1);
		}
		return differentValuesCounter <= 1;
	}

	private boolean afterAndSuperUseMoreThanOneVariableOfBefore(FragmentsSplitter fragmentsSplitter) {
		final Collection<VariableDeclarationExpr> variables = fragmentsSplitter
				.getBeforeVariablesUsedInSpecificNodeAndBeforeFragments();
		return variables.size() > 1 || (variables.size() == 1 && variables.stream().findFirst().get().getVariables().size() > 1);
	}

	private boolean beforeFragmentHasNoReturn(FragmentsSplitter fragmentsSplitter) {
		return fragmentsSplitter.getBeforeFragment().stream()
				.allMatch(node -> !this.astHandler.nodeHasReturnStatement(node));
	}

	private boolean beforeFragmentThrowsNoException(FragmentsSplitter fragmentsSplitter) {
		return fragmentsSplitter.getBeforeFragment().stream()
				.allMatch(node -> !this.astHandler.nodeThrowsException(node));
	}

	private boolean superCallIsNotNested(MethodDeclaration m, SuperExpr superCall) {

		final Optional<BlockStmt> blockStmt = this.astHandler.getBlockStatement(m);

		if (blockStmt.isPresent()) {
			return this.astHandler.childHasDirectSuperCall(blockStmt.get(), superCall);
		}

		return false;
	}

}
