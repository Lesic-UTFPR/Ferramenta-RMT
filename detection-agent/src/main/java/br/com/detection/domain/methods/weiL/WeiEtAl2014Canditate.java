package br.com.detection.domain.methods.weiL;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;

import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public abstract class WeiEtAl2014Canditate implements RefactoringCandidate {

	private final String id = UUID.randomUUID().toString();

	private final Reference reference;

	private final Path file;

	private final CompilationUnit compilationUnit;

	private final PackageDeclaration packageDcl;

	private final ClassOrInterfaceDeclaration classDcl;

	private final MethodDeclaration methodDcl;

	private final List<IfStmt> ifStatements = new ArrayList<>();

	private final DesignPattern eligiblePattern;

	public WeiEtAl2014Canditate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration methodDcl,
			Collection<IfStmt> ifStatements, DesignPattern eligiblePattern) {
		this.reference = reference;
		this.file = file;
		this.compilationUnit = compilationUnit;
		this.packageDcl = packageDcl;
		this.classDcl = classDcl;
		this.methodDcl = methodDcl;
		this.ifStatements.addAll(ifStatements);
		this.eligiblePattern = eligiblePattern;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Reference getReference() {
		return reference;
	}

	@Override
	public String getPkg() {
		return this.packageDcl.getNameAsString();
	}

	@Override
	public String getClassName() {
		return this.file.getFileName().toString();
	}

	@Override
	public DesignPattern getEligiblePattern() {
		return eligiblePattern;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public PackageDeclaration getPackageDeclaration() {
		return packageDcl;
	}

	public ClassOrInterfaceDeclaration getClassDeclaration() {
		return classDcl;
	}

	public MethodDeclaration getMethodDcl() {
		return methodDcl;
	}

	public List<IfStmt> getIfStatements() {
		return ifStatements;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeiEtAl2014Canditate) {
			WeiEtAl2014Canditate another = (WeiEtAl2014Canditate) object;
			return new EqualsBuilder().append(id, another.id).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

}
