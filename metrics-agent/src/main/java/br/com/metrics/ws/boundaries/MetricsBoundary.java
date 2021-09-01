package br.com.metrics.ws.boundaries;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.metrics.MetricsAgentApi;
import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;
import br.com.metrics.domain.files.FileRepositoryCollections;
import br.com.metrics.managers.pulse.PulseManager;
import br.com.metrics.processors.qualityAttributes.QualityAttributesProcessor;
import br.com.metrics.repositories.project.ProjectsReadonlyRepository;

@Path(MetricsAgentApi.ROOT)
@Produces(RestPatterns.PRODUCES_JSON)
@Consumes(RestPatterns.CONSUMES_JSON)
public class MetricsBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private @Inject QualityAttributesProcessor processor;

	private @Inject ProjectsReadonlyRepository projectsRepository;
	
	private @Inject PulseManager pulseManager;

	@GET
	@Path(MetricsAgentApi.PROJECT_PARAM)
	public Collection<QualityAttributeResult> evaluate(@PathParam("projectId") String projectId,
			@PathParam("refactoredProjectId") String refactoredProjectId) {

		final Project project = this.projectsRepository.get(FileRepositoryCollections.PROJECTS, projectId)
				.orElseThrow(IllegalArgumentException::new);

		final Project refactoredProject = this.projectsRepository
				.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredProjectId)
				.orElseThrow(IllegalArgumentException::new);

		return processor.extract(project, refactoredProject);
	}
	
	@POST
	@Path(MetricsAgentApi.FORCE_REGISTRATION)
	public void forceRegistration() {
		this.pulseManager.registerAsMember();
	}

}
