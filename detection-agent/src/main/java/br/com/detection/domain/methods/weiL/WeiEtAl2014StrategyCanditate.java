package br.com.detection.domain.methods.weiL;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.IfStmt;

import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public class WeiEtAl2014StrategyCanditate extends WeiEtAl2014Canditate {

	private final List<VariableDeclarator> variables;

	public WeiEtAl2014StrategyCanditate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
			Collection<IfStmt> ifStatements, List<VariableDeclarator> variables) {
		super(reference, file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements, DesignPattern.STRATEGY);
		this.variables = variables;
	}

	public List<VariableDeclarator> getVariables() {
		return variables;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014StrategyCanditate) {
			WeiEtAl2014StrategyCanditate another = (WeiEtAl2014StrategyCanditate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
