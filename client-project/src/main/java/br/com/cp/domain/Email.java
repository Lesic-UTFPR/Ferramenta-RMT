package br.com.cp.domain;

public class Email implements Contact {
	
	private String email;
	
	@Override
	public String display() {
		return email;
	}

}
