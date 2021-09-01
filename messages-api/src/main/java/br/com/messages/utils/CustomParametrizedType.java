package br.com.messages.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CustomParametrizedType implements ParameterizedType {

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
