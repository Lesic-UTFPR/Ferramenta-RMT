package br.com.metrics.domain.metrics.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ClassInfo extends ASTVisitor {

	private String className;
	private String type;

	@Override
	public boolean visit(TypeDeclaration node) {

		getFullClassName(node.resolveBinding());

		if (node.isInterface())
			type = "interface";
		else
			type = "class";

		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		type = "enum";
		getFullClassName(node.resolveBinding());
		return false;
	}

	public String getClassName() {
		return className;
	}

	public String getType() {
		return type;
	}

	private void getFullClassName(ITypeBinding binding) {
		if (binding != null)
			this.className = binding.getBinaryName();
	}

}
