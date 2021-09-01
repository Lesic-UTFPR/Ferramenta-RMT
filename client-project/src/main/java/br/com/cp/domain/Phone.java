package br.com.cp.domain;

public class Phone implements Contact {
	
	private int country;
	
	private int code;
	
	private long number;

	@Override
	public String display() {
//		return String.format("%d %d, %d", country, code, number);
		throw new IllegalArgumentException();
	}

}
