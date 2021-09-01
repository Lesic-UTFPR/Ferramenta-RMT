package br.com.messages.members.api.intermediary;

public class IntermediaryAgentProjectsApi {

	public static final String ROOT = "/projects";

	public static final String TEST = "/test";

	public static final String REGISTRATION = "/register";
	
	public static final String REGISTRATION_WITH_PARAM = "/register/{name}/{contentType}";

	public static final String EVALUATE = "/evaluate";
	
	public static final String EVALUATE_WITH_PARAM = "/evaluate/{projectId}";
	
	public static final String APPLY_PATTERNS = "/apply-patterns";
	
	public static final String APPLY_PATTERNS_WITH_PARAM = "/apply-patterns/{projectId}";
	
	public static final String GET_PHASE = "/getPhase";
	
	public static final String GET_EVALUATION_RESULTS = "/getEvaluationResults";
	
	public static final String PROJECT_ID_PARAM = "projectId";

}
