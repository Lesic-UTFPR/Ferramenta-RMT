package br.com.intermediary.ws.boundaries;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import br.com.intermediary.files.collections.FileRepositoryCollections;
import br.com.intermediary.files.projects.ProjectsRepository;
import br.com.intermediary.managers.members.MembersManager;
import br.com.intermediary.managers.projects.ProjectsManager;
import br.com.intermediary.managers.projects.exceptions.evaluation.ProjectEvaluationException;
import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.intermediary.IntermediaryAgentProjectsApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.candidates.evaluation.EvaluationDTO;
import br.com.messages.projects.Project;

@Stateless
@Path(IntermediaryAgentProjectsApi.ROOT)
@Produces(RestPatterns.PRODUCES_JSON)
@Consumes(RestPatterns.CONSUMES_JSON)
public class IntermediaryBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private final FileRepositoryCollections PROJECTS_COLLECTION = FileRepositoryCollections.PROJECTS;

	private @Inject ProjectsManager projectsManager;

	private @Inject MembersManager membersManager;

	private @Inject ProjectsRepository projectsRepository;

	@GET
	@Path(IntermediaryAgentProjectsApi.TEST)
	public Integer test() {

		System.out.println("Test Projects!");

		return 1;
	}

	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Path(IntermediaryAgentProjectsApi.REGISTRATION_WITH_PARAM)
	@POST
	public String registration(@PathParam("name") String name, @PathParam("contentType") String contentType,
			@NotNull InputStream inputStream, @Context UriInfo uriInfo) throws IOException {

		try {
			Project project = new Project(UUID.randomUUID().toString(), name, () -> inputStream,
					new String(Base64.getDecoder().decode(contentType), "UTF-8"));

			projectsRepository.put(PROJECTS_COLLECTION, project);

			this.projectsManager.register((project = new Project(project.getId(), project.getName())));

			return project.getId();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException();
		}
	}

	@GET
	@Path(IntermediaryAgentProjectsApi.EVALUATE_WITH_PARAM)
	public Collection<RefactoringCandidadeDTO> evaluation(@PathParam("projectId") String projectId) {

		final Collection<RefactoringCandidadeDTO> result = new ArrayList<>();

		final Collection<RefactoringCandidadeDTO> candidates = this.projectsManager
				.evaluate(membersManager::getNextDetector, projectId).stream().distinct().collect(Collectors.toList());

		for (RefactoringCandidadeDTO candidate : candidates) {
			final String refactoredProjId = this.projectsManager.refactor(membersManager::getNextDetector, projectId,
					Stream.of(candidate).collect(Collectors.toList()));

			candidate.setEvaluation(new EvaluationDTO(
					this.projectsManager.evaluate(membersManager::getNextMetrics, projectId, refactoredProjId)));

			result.add(candidate);
		}
		
		System.out.println(String.format("Fim avaliação com %d candidatos", result.size()));

		return result;
	}

	@POST
	@Path(IntermediaryAgentProjectsApi.APPLY_PATTERNS_WITH_PARAM)
	public StreamingOutput applyPatterns(@Context HttpServletResponse response,
			@PathParam("projectId") String projectId, List<RefactoringCandidadeDTO> candidates)
			throws ProjectEvaluationException {

		final String refactoredId = this.projectsManager.refactor(() -> membersManager.getNextDetector(), projectId,
				candidates);

		final Project project = this.projectsRepository.get(FileRepositoryCollections.REFACTORED_PROJECTS, refactoredId)
				.orElseThrow(IllegalStateException::new);

		response.setContentType(project.getContentType());

		if (!StringUtils.isEmpty(project.getName())) {
			final String fileName = "attachment; filename="
					.concat(project.getName().replace(".zip", "").concat(".zip"));

			response.setHeader("Content-Disposition", fileName);
		}

		return project::sendContentTo;
	}

}
