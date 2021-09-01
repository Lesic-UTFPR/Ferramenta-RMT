package br.com.detection.domain.methods.weiL;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public class WeiEtAl2014FactoryCanditate extends WeiEtAl2014Canditate {

	private final ClassOrInterfaceType methodReturnType;

	public WeiEtAl2014FactoryCanditate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
			ClassOrInterfaceType methodReturnType, Collection<IfStmt> ifStatements) {
		super(reference, file, compilationUnit, packageDcl, classDcl, methodDcl, ifStatements,
				DesignPattern.FACTORY_METHOD);

		this.methodReturnType = methodReturnType;
	}

	public ClassOrInterfaceType getMethodReturnType() {
		return methodReturnType;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014FactoryCanditate) {
			WeiEtAl2014FactoryCanditate another = (WeiEtAl2014FactoryCanditate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
