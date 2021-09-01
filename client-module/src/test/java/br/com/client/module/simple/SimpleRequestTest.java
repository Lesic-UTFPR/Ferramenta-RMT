package br.com.client.module.simple;

import org.junit.Assert;

import br.com.client.module.utils.ClientTarget;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.members.api.intermediary.IntermediaryAgentProjectsApi;

public class SimpleRequestTest {

	// @Test
	public void testIntermediaryAgentCommunication() {
		final String path = String.format("%s%s%s", IntermediaryAgentCoreApi.AGENT_PATH,
				IntermediaryAgentProjectsApi.ROOT, IntermediaryAgentProjectsApi.TEST);

		final Integer result = new ClientTarget().get(path).request().get(Integer.class);

		Assert.assertTrue(1 == result);
	}

}
