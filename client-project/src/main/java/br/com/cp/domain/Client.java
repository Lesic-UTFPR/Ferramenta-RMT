package br.com.cp.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Client {
	
	private String name;
	
	private LocalDate birthDate;
	
	private List<Contact> contacts = new ArrayList<>();

}
