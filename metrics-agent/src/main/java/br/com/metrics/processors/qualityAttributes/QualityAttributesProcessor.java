package br.com.metrics.processors.qualityAttributes;

import java.util.Collection;

import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;

public interface QualityAttributesProcessor {

	Collection<QualityAttributeResult> extract(Project project, Project refactoredProject);

}
