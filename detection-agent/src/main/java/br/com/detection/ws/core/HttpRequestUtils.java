package br.com.detection.ws.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import br.com.messages.addresses.IntermediaryAgentAddress;

public class HttpRequestUtils {

	public static WebTarget getTarget(String path) {

		final String target = String.format("http://%s:%s", IntermediaryAgentAddress.HOST,
				IntermediaryAgentAddress.PORT);

		final WebTarget wt = ClientBuilder.newClient().register(DetectionAgentObjectMapperProvider.class).target(target)
				.path(path);

		System.out.println(String.format("Request (%s) ", wt.getUri()));

		return wt;
	}

	public static String createPath(String... steps) {
		final StringBuilder builder = new StringBuilder();
		for (String s : steps) {
			builder.append(s);
		}
		return builder.toString();
	}

	public static class CustomParametrizedType implements ParameterizedType {

		private final Class<?> rawType;
		private final Type[] parameterTypes;

		public CustomParametrizedType(Class<?> rawType, Type... parameterTypes) {
			this.rawType = rawType;
			this.parameterTypes = parameterTypes;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return this.parameterTypes;
		}

		@Override
		public Type getRawType() {
			return this.rawType;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}

	}

}
