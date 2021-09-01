package br.com.metrics.domain.metrics.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import br.com.metrics.domain.metrics.report.CKNumber;
import br.com.metrics.domain.metrics.report.CKReport;

public class LCOMProcessor extends ASTVisitor implements MetricProcessor {

	private final ArrayList<TreeSet<String>> methods = new ArrayList<TreeSet<String>>();
	private final Set<String> declaredFields = new HashSet<String>();

	public boolean visit(FieldDeclaration node) {
		for (Object o : node.fragments()) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
			declaredFields.add(vdf.getName().toString());
		}
		return super.visit(node);
	}

	public boolean visit(SimpleName node) {
		if (declaredFields.contains(node.getFullyQualifiedName())) {
			acessed(node.getFullyQualifiedName());
		}
		return super.visit(node);
	}

	private void acessed(String name) {
		if (!methods.isEmpty()) {
			methods.get(methods.size() - 1).add(name);
		}
	}

	public boolean visit(MethodDeclaration node) {
		methods.add(new TreeSet<String>());

		return super.visit(node);
	}

	@Override
	public void execute(CompilationUnit cu, CKNumber number, CKReport report) {
		cu.accept(this);
	}

	@Override
	public void setResult(CKNumber result) {

		/*
		 * LCOM = |P| - |Q| if |P| - |Q| > 0 where P = set of all empty set
		 * intersections Q = set of all nonempty set intersections
		 */

		// extracted from https://github.com/dspinellis/ckjm
		int lcom = 0;
		for (int i = 0; i < methods.size(); i++) {
			for (int j = i + 1; j < methods.size(); j++) {

				final TreeSet<?> intersection = (TreeSet<?>) methods.get(i).clone();
				intersection.retainAll(methods.get(j));
				
				lcom = (intersection.size() == 0) ? (lcom + 1) : (lcom - 1);
			}
		}
			
		result.setLcom(lcom > 0 ? lcom : 0);
	}

}
