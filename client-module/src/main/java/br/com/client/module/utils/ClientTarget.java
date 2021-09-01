package br.com.client.module.utils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import br.com.messages.addresses.IntermediaryAgentAddress;

public class ClientTarget {

	public WebTarget get(String path) {

		final String target = String.format("http://%s:%s", IntermediaryAgentAddress.HOST,
				IntermediaryAgentAddress.PORT);

		return ClientBuilder.newClient().register(ClientModuleObjectMapperProvider.class).target(target).path(path);
	}

}
