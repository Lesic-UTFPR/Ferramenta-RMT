package br.com.detection.domain.methods.zeiferisVE.preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate;
import br.com.detection.domain.methods.zeiferisVE.ZafeirisEtAl2016Canditate.CandidateWithVariables;

public class SiblingPreconditions {

	private final AstHandler astHandler = new AstHandler();

	public boolean violates(Collection<ZafeirisEtAl2016Canditate> canditadesOfSameOverridenMethod) {
		final List<CandidateWithVariables> candidatesWithVariables = canditadesOfSameOverridenMethod.stream()
				.map(ZafeirisEtAl2016Canditate::toCandidateWithVariables).collect(Collectors.toList());

		return !beforeFragmentReturnIsSame(candidatesWithVariables)
				&& !this.beforeReturnIsUsedInSuper(candidatesWithVariables)
				&& this.isHierarchyShort(candidatesWithVariables);
	}

	private boolean isHierarchyShort(List<CandidateWithVariables> candidatesWithVariables) {

		final List<Hierarchy> hierarchies = new ArrayList<>();

		for (CandidateWithVariables candidate : candidatesWithVariables) {
			boolean isPartOfAny = false;
			for (int i = 0; i < hierarchies.size(); i++) {
				if (hierarchies.get(i).isPartOf(candidate.getCandidate().getClassDeclaration())) {
					isPartOfAny = true;
					hierarchies.get(i).declarations.add(candidate.getCandidate().getClassDeclaration());
				}
			}

			if (!isPartOfAny) {
				hierarchies.add(new Hierarchy());
				hierarchies.get(0).declarations.add(candidate.getCandidate().getClassDeclaration());
			}
		}

		return hierarchies.stream().anyMatch(h -> h.declarations.size() < 2);
	}

	private boolean beforeFragmentReturnIsSame(List<CandidateWithVariables> candidatesWithVariables) {

		boolean areEqual = true;
		for (int i = 1; i < candidatesWithVariables.size() - 1; i++) {
			if (candidatesWithVariables.get(i).getVariables().size() > 1) {
				throw new IllegalStateException();
			} else if (candidatesWithVariables.get(i).getVariables().size() != candidatesWithVariables.get(i + 1)
					.getVariables().size()) {
				areEqual &= false;
			} else if (candidatesWithVariables.get(i).getVariables().size() == 0) {
				areEqual &= false;
			} else {
				areEqual &= this.astHandler.variablesAreEqual(
						candidatesWithVariables.get(i).getVariables().stream().findFirst().get(),
						candidatesWithVariables.get(i + 1).getVariables().stream().findFirst().get());
			}
		}
		return areEqual;
	}

	private boolean beforeReturnIsUsedInSuper(List<CandidateWithVariables> candidatesWithVariables) {
		if (candidatesWithVariables.stream().findFirst().get().getVariables().size() == 0) {
			return true;
		}
		boolean isUsed = true;
		for (CandidateWithVariables candidate : candidatesWithVariables) {
			final VariableDeclarationExpr var = candidate.getVariables().stream().findFirst().get();

			final MethodCallExpr methodCall = (MethodCallExpr) candidate.getCandidate().getSuperCall().getParentNode()
					.get();

			isUsed &= this.astHandler.variableIsPresentInMethodCall(var, methodCall);
		}
		return isUsed;
	}

	private class Hierarchy {
		final Set<ClassOrInterfaceDeclaration> declarations = new HashSet<>();

		boolean isPartOf(ClassOrInterfaceDeclaration dclr) {
			return declarations.contains(dclr) && this.isChildOfAny(dclr) && this.isParentOfAny(dclr);
		}

		boolean isChildOfAny(ClassOrInterfaceDeclaration dclr) {
			final Optional<ClassOrInterfaceType> parent = astHandler.getParentType(dclr);
			if (!parent.isPresent()) {
				return false;
			}
			return declarations.stream().map(astHandler::getSimpleName).map(Optional::get)
					.anyMatch(n -> n.equals(astHandler.getSimpleName(parent.get()).get()));
		}

		boolean isParentOfAny(ClassOrInterfaceDeclaration dclr) {
			return this.declarations.stream().map(astHandler::getParentType).filter(Optional::isPresent)
					.map(Optional::get).map(astHandler::getSimpleName).map(Optional::get)
					.anyMatch(n -> n.equals(astHandler.getSimpleName(dclr).get()));
		}

	}

}
