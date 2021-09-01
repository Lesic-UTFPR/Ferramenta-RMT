package br.com.client.module.screens.projects;

import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

public class RefactoringCandidateSelector implements RefactoringCandidate {

	private BooleanProperty selected = new SimpleBooleanProperty(false);

	private final RefactoringCandidadeDTO dto;

	public RefactoringCandidateSelector(RefactoringCandidadeDTO dto) {
		this.dto = dto;
	}

	public ObservableBooleanValue isSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected.set(selected == null ? false : selected);
	}
	
	public RefactoringCandidadeDTO getDto() {
		return dto;
	}

	@Override
	public String getId() {
		return dto.getId();
	}

	@Override
	public Reference getReference() {
		return dto.getReference();
	}

	@Override
	public String getPkg() {
		return dto.getPkg();
	}

	@Override
	public String getClassName() {
		return dto.getClassName();
	}

	@Override
	public DesignPattern getEligiblePattern() {
		return dto.getEligiblePattern();
	}

}
