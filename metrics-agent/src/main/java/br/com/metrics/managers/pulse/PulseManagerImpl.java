package br.com.metrics.managers.pulse;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.RestPatterns;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.members.api.intermediary.IntermediaryAgentPulsesApi;
import br.com.messages.pulses.Pulse;
import br.com.metrics.domain.identity.Identity;
import br.com.metrics.ws.core.HttpRequestUtils;

@Singleton
public class PulseManagerImpl implements PulseManager {

	private static final long serialVersionUID = 1L;

	private final Pulse pulse = new Pulse(Identity.ID, MemberType.PATTERNS_METRICS_EVALUATOR);

	@Schedule(hour = "*", minute = "*", second = "1", persistent = false)
	public void sendPulse() {

		final String path = HttpRequestUtils.createPath(IntermediaryAgentCoreApi.AGENT_PATH,
				IntermediaryAgentPulsesApi.ROOT, IntermediaryAgentPulsesApi.RENEW);

		System.out.println("Metrics - Sending pulse. Beggining registration...");

		final Response response = HttpRequestUtils.getTarget(path).request(RestPatterns.PRODUCES_JSON)
				.post(Entity.json(pulse), Response.class);

		if (Response.Status.PRECONDITION_FAILED.getStatusCode() == response.getStatus()) {
			registerAsMember();
		} else if (Response.Status.OK.getStatusCode() == response.getStatus()) {
			System.out.println("Metrics - Pulse accepted.");
		} else {
			System.out.println(
					String.format("Metrics - Response not identified - ", ToStringBuilder.reflectionToString(response)));
		}
	}

	public void registerAsMember() {

		try {
			final Member member = Identity.getAsMember();

			System.out.println(String.format("Metrics - Membro %s não registrado. Iniciando registro...",
					member.getMemberId()));

			final String path = HttpRequestUtils.createPath(IntermediaryAgentCoreApi.AGENT_PATH,
					IntermediaryAgentPulsesApi.ROOT, IntermediaryAgentPulsesApi.REGISTRATION);

			System.out.println(ToStringBuilder.reflectionToString(member));

			final Response response = HttpRequestUtils.getTarget(path).request(RestPatterns.PRODUCES_JSON)
					.post(Entity.json(member), Response.class);

			if (Response.Status.OK.getStatusCode() != response.getStatus()) {
				System.out.println("Metrics - Registration Response: " + ToStringBuilder.reflectionToString(response));
				System.out.println(
						"Metrics - Registration Response Entity: " + ToStringBuilder.reflectionToString(response.getEntity()));
			} else {
				System.out.println("Metrics - Registration Succeded!");
			}
		} catch (Exception e) {
			System.out.println("Metrics - Failed while registering - " + ToStringBuilder.reflectionToString(e));
		}
	}

}
