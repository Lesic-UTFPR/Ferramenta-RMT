package br.com.client.module.services;

public class Identity {

	private static String ID;

	private static final String NAME = "Project";

	public static String getID() {
		return ID;
	}

	public static void setID(String iD) {
		ID = iD;
	}

	public static String getName() {
		return NAME;
	}

}
