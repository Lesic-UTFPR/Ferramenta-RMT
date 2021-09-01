package br.com.intermediary.managers.projects;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import br.com.intermediary.utils.HttpRequestUtils;
import br.com.messages.members.Member;
import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.api.metrics.MetricsAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.metrics.QualityAttributeResultDTO;
import br.com.messages.projects.Project;
import br.com.messages.utils.CustomParametrizedType;

@Stateless
public class ProjectsManagerImpl implements ProjectsManager {

	private static final long serialVersionUID = 1L;

	private @Inject ProjectsPool projectsPool;

	@Override
	public void register(Project project) {
		projectsPool.register(project);
	}

	@Override
	public Collection<Project> getProjects() {
		return projectsPool.getAll();
	}

	@Override
	public List<RefactoringCandidadeDTO> evaluate(Supplier<Member> detector, String projectId) {

		checkId(projectId);

		final String path = HttpRequestUtils.createPath(DetectionAgentApi.DETECTION_PATH, DetectionAgentApi.ROOT,
				DetectionAgentApi.START_DETECTION, projectId.trim());

		return HttpRequestUtils.getTarget(detector.get().getHost(), detector.get().getPort(), path).request()
				.get(new GenericType<List<RefactoringCandidadeDTO>>(
						new CustomParametrizedType(List.class, RefactoringCandidadeDTO.class)));
	}

	@Override
	public String refactor(Supplier<Member> detector, String projectId, List<RefactoringCandidadeDTO> candidates) {

		checkId(projectId);

		final String path = HttpRequestUtils.createPath(DetectionAgentApi.DETECTION_PATH, DetectionAgentApi.ROOT,
				DetectionAgentApi.REFACTOR, projectId.trim());

		return HttpRequestUtils.getTarget(detector.get().getHost(), detector.get().getPort(), path).request()
				.post(Entity.entity(candidates, MediaType.APPLICATION_JSON), String.class);
	}

	@Override
	public Collection<QualityAttributeResultDTO> evaluate(Supplier<Member> metrics, String projectId,
			String refactoredProjId) {

		checkId(projectId);
		checkId(refactoredProjId);

		final String path = HttpRequestUtils.createPath(MetricsAgentApi.METRICS_PATH, MetricsAgentApi.ROOT, projectId);

		return HttpRequestUtils.getTarget(metrics.get().getHost(), metrics.get().getPort(), path.concat("/"+refactoredProjId)).request()
				.get(new GenericType<List<QualityAttributeResultDTO>>(
						new CustomParametrizedType(List.class, QualityAttributeResultDTO.class)));
	}

	@Override
	public Optional<Project> getProjectById(String id) {
		return projectsPool.getById(id);
	}

	private void checkId(String id) {
		if (id == null || id.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}
	}

}
