package br.com.detection.domain.methods.zeiferisVE;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public class ZafeirisEtAl2016Canditate implements RefactoringCandidate {

	private final String id = UUID.randomUUID().toString();

	private final Reference reference;

	private final Path file;

	private final CompilationUnit compilationUnit;

	private final PackageDeclaration packageDcl;

	private final ClassOrInterfaceDeclaration classDcl;

	private final MethodDeclaration overridenMethod;

	private final MethodDeclaration overridingMethod;

	private final SuperExpr superCall;

	public ZafeirisEtAl2016Canditate(Reference reference, Path file, CompilationUnit compilationUnit,
			PackageDeclaration packageDcl, ClassOrInterfaceDeclaration classDcl, MethodDeclaration overridenMethod,
			MethodDeclaration overridingMethod, SuperExpr superCall) {
		this.reference = reference;
		this.file = file;
		this.compilationUnit = compilationUnit;
		this.packageDcl = packageDcl;
		this.classDcl = classDcl;
		this.overridenMethod = overridenMethod;
		this.overridingMethod = overridingMethod;
		this.superCall = superCall;
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
		return DesignPattern.TEMPLATE_METHOD;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public MethodDeclaration getOverridingMethod() {
		return overridingMethod;
	}

	public MethodDeclaration getOverridenMethod() {
		return overridenMethod;
	}

	public PackageDeclaration getPackageDeclaration() {
		return packageDcl;
	}

	public ClassOrInterfaceDeclaration getClassDeclaration() {
		return classDcl;
	}

	public SuperExpr getSuperCall() {
		return superCall;
	}

	public FragmentsSplitter toFragment() {
		return new FragmentsSplitter(this.getOverridingMethod(), this.getSuperCall());
	}

	public CandidateWithVariables toCandidateWithVariables() {
		return new CandidateWithVariables(this,
				this.toFragment().getBeforeVariablesUsedInSpecificNodeAndBeforeFragments());
	}

	public class CandidateWithVariables {

		private final ZafeirisEtAl2016Canditate candidate;

		private final Collection<VariableDeclarationExpr> variables;

		public CandidateWithVariables(ZafeirisEtAl2016Canditate candidate,
				Collection<VariableDeclarationExpr> variables) {
			this.candidate = candidate;
			this.variables = variables;
		}

		public ZafeirisEtAl2016Canditate getCandidate() {
			return candidate;
		}

		public Collection<VariableDeclarationExpr> getVariables() {
			return variables;
		}

	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ZafeirisEtAl2016Canditate) {
			ZafeirisEtAl2016Canditate another = (ZafeirisEtAl2016Canditate) object;
			return new EqualsBuilder().append(id, another.id).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

}
