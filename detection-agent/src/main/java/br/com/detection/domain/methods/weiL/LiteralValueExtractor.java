package br.com.detection.domain.methods.weiL;

import java.util.Optional;
import java.util.function.Predicate;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;

public class LiteralValueExtractor {

	private final AstHandler astHandler = new AstHandler();

	public Optional<Object> getNodeOtherThan(Node node, Parameter parameter) {
		final Predicate<Node> isAValidChild = (c) -> !this.astHandler.getNameExpr(c)
				.map(n -> n.getNameAsString().equals(parameter.getNameAsString())).isPresent();

		return node.getChildNodes().stream().filter(isAValidChild).filter(LiteralExpr.class::isInstance)
				.map(LiteralExpr.class::cast).map(this::extractLiteralValidValues).filter(Optional::isPresent)
				.map(Optional::get).findFirst();
	}

	private Optional<Object> extractLiteralValidValues(LiteralExpr expr) {
		if (expr instanceof LiteralStringValueExpr) {
			return Optional.ofNullable(((LiteralStringValueExpr) expr).getValue());
		} else if (expr instanceof CharLiteralExpr) {
			return Optional.ofNullable(((CharLiteralExpr) expr).getValue());
		} else if (expr instanceof IntegerLiteralExpr) {
			return Optional.ofNullable(((IntegerLiteralExpr) expr).getValue());
		} else if (expr instanceof LongLiteralExpr) {
			return Optional.ofNullable(((LongLiteralExpr) expr).getValue());
		}
		return Optional.empty();
	}

}
