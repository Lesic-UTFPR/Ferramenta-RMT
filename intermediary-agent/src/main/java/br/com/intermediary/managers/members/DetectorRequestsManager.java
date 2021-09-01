package br.com.intermediary.managers.members;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.builder.EqualsBuilder;

import br.com.intermediary.utils.HttpRequestUtils;
import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.utils.CustomParametrizedType;

public class DetectorRequestsManager {

	public List<Reference> getReferences(Member member) {
		if (!new EqualsBuilder().append(MemberType.PATTERNS_SPOTS_DETECTOR, member.getMemberType()).isEquals()) {
			return Collections.emptyList();
		}

		final String path = HttpRequestUtils.createPath(DetectionAgentApi.DETECTION_PATH, DetectionAgentApi.ROOT,
				DetectionAgentApi.RETRIEVE_REFERENCES);

		return HttpRequestUtils.getTarget(member.getHost(), member.getPort(), path).request()
				.get(new GenericType<List<Reference>>(new CustomParametrizedType(List.class, Reference.class)));
	}

	public List<RefactoringCandidadeDTO> startDetection(Member member, String projectId) {
		if (projectId == null || projectId.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}

		final String path = HttpRequestUtils.createPath(DetectionAgentApi.DETECTION_PATH, DetectionAgentApi.ROOT,
				DetectionAgentApi.START_DETECTION, projectId.trim());

		return HttpRequestUtils.getTarget(member.getHost(), member.getPort(), path).request()
				.get(new GenericType<List<RefactoringCandidadeDTO>>(
						new CustomParametrizedType(List.class, RefactoringCandidadeDTO.class)));
	}

	public String startRefactoring(Member member, String projectId, List<RefactoringCandidadeDTO> candidates) {
		if (projectId == null || projectId.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}

		final String path = HttpRequestUtils.createPath(DetectionAgentApi.DETECTION_PATH, DetectionAgentApi.ROOT,
				DetectionAgentApi.REFACTOR, projectId.trim());

		return HttpRequestUtils.getTarget(member.getHost(), member.getPort(), path).request()
				.post(Entity.entity(candidates, MediaType.APPLICATION_JSON), String.class);
	}

}
