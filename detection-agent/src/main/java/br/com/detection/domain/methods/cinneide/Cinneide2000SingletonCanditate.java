package br.com.detection.domain.methods.cinneide;

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
import com.github.javaparser.ast.type.Type;

import br.com.detection.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public class Cinneide2000SingletonCanditate extends Cinneide2000Candidate{
	
	private final ClassOrInterfaceType methodReturnType;
	
	public Cinneide2000SingletonCanditate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
			ClassOrInterfaceType methodReturnType) {
		super(reference, file, compilationUnit, packageDcl, classDcl, methodDcl, DesignPattern.SINGLETON);
		
		this.methodReturnType = methodReturnType;
	}
	
	public ClassOrInterfaceType getMethodRetrunType() {
		return methodReturnType;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Cinneide2000SingletonCanditate) {
			Cinneide2000SingletonCanditate another = (Cinneide2000SingletonCanditate) object;
			return new EqualsBuilder().append(this.getId(), another.getId()).isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).toHashCode();
	}

}
