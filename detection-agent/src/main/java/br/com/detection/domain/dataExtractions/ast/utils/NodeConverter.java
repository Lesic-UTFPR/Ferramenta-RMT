package br.com.detection.domain.dataExtractions.ast.utils;

import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;

import io.bretty.console.tree.TreeNodeConverter;
import io.bretty.console.tree.TreePrinter;

public class NodeConverter implements TreeNodeConverter<Node> {
	@Override
	public String name(Node file) {
		if(file instanceof Name) {
			return String.format("%s - %s", file.getMetaModel().getTypeName(), ((Name) file).asString());
		} 
		if(file instanceof SimpleName) {
			return String.format("%s - %s", file.getMetaModel().getTypeName(), ((SimpleName) file).asString());
		}
		return file == null ? "" : String.format("%s", file.getMetaModel().getTypeName());
	}

	@Override
	public List<Node> children(Node file) {
		return file == null ? Collections.emptyList() :file.getChildNodes();
	}
	
	public static String toString(Node node) {
		return TreePrinter.toString(node, new NodeConverter());
	}
}