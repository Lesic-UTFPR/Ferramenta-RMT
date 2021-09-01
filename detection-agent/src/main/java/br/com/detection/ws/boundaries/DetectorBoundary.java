package br.com.detection.ws.boundaries;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import br.com.detection.managers.pulse.PulseManager;
import br.com.detection.methods.DetectionMethodsManager;
import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.detectors.DetectionAgentApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.utils.JsonUtils;

@Path(DetectionAgentApi.ROOT)
@Produces(RestPatterns.PRODUCES_JSON)
@Consumes(RestPatterns.CONSUMES_JSON)
public class DetectorBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private @Inject DetectionMethodsManager detectionMethodsManager;
	
	private @Inject PulseManager pulseManager;

	/**
	 * End point that receives a HTTP GET request. 
	 * It calls the detectionMethodsManager.
	 * @param projectId
	 * @return Returns a JsonArray with the refactoring candidates. 
	 */
	@GET
	@Path(DetectionAgentApi.START_DETECTION_WITH_PARAMS)
	public JsonArray requestEvaluation(@PathParam("projectId") String projectId) {
		final JsonArrayBuilder builder = Json.createArrayBuilder();

		detectionMethodsManager.extractCandidates(projectId).stream().map(JsonUtils::toJson).forEach(builder::add);

		return builder.build();
	}

	@POST
	@Path(DetectionAgentApi.REFACTOR_WITH_PARAMS)
	public String applyPatterns(@PathParam("projectId") String projectId, Collection<RefactoringCandidadeDTO> eligiblePatterns) {
		final String refactoredProjectId = detectionMethodsManager.refactor(projectId, eligiblePatterns);
		return refactoredProjectId;
	}

	@GET
	@Path(DetectionAgentApi.RETRIEVE_REFERENCES)
	public JsonArray getReferences() {
		final JsonArrayBuilder builder = Json.createArrayBuilder();

		this.detectionMethodsManager.getReferences().stream().map(JsonUtils::toJson).forEach(builder::add);

		return builder.build();
	}
	
	@POST
	@Path(DetectionAgentApi.FORCE_REGISTRATION)
	public void forceRegistration() {
		this.pulseManager.registerAsMember();
	}

}
