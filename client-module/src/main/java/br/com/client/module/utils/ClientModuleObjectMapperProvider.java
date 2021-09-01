package br.com.client.module.utils;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ClientModuleObjectMapperProvider implements ContextResolver<ObjectMapper> {
	ObjectMapper mapper;

	public ClientModuleObjectMapperProvider() {
		mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}
