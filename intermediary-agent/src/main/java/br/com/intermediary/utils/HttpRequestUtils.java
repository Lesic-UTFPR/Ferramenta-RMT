package br.com.intermediary.utils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import br.com.intermediary.ws.core.IntermediaryAgentObjectMapperProvider;

public class HttpRequestUtils {

	public static WebTarget getTarget(String host, String port, String path) {

		final String target = String.format("http://%s:%s", host, port);

		final WebTarget wt = ClientBuilder.newClient().register(IntermediaryAgentObjectMapperProvider.class).target(target).path(path);

//		System.out.println("Request - " + wt.getUri());

		return wt;
	}

	public static String createPath(String... steps) {
		final StringBuilder builder = new StringBuilder();
		for (String s : steps) {
			builder.append(s);
		}
		return builder.toString();
	}
	
}
