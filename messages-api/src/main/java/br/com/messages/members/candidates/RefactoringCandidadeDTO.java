package br.com.messages.members.candidates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import br.com.messages.members.candidates.evaluation.EvaluationDTO;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public class RefactoringCandidadeDTO implements RefactoringCandidate {

	private final String id;

	private final Reference reference;

	private final String pkg;

	private final String className;

	private final DesignPattern eligiblePattern;

	private EvaluationDTO evaluation = null;

	public RefactoringCandidadeDTO() {
		this(null, null, null, null, null);
	}

	public RefactoringCandidadeDTO(String id, Reference reference, String pkg, String className,
			DesignPattern eligiblePattern) {
		this.id = id;
		this.reference = reference;
		this.pkg = pkg;
		this.className = className;
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
		return pkg;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public DesignPattern getEligiblePattern() {
		return eligiblePattern;
	}

	public boolean hasEvaluation() {
		return this.evaluation != null;
	}

	public void setEvaluation(EvaluationDTO evaluation) {
		this.evaluation = evaluation;
	}

	public EvaluationDTO getEvaluation() {
		return evaluation;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RefactoringCandidadeDTO) {
			RefactoringCandidadeDTO another = (RefactoringCandidadeDTO) object;
			return new EqualsBuilder().append(className, another.className).append(this.pkg, another.pkg)
					.append(this.eligiblePattern, another.eligiblePattern).append(this.reference.getTitle(), another.reference.getTitle())
					.append(this.reference.getYear(), another.reference.getYear()).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

}
