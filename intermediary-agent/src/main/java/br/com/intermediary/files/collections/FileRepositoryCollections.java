package br.com.intermediary.files.collections;

public enum FileRepositoryCollections {
	PROJECTS("projects"), REFACTORED_PROJECTS("refactored-projects");

	private final String id;

	private FileRepositoryCollections(String id) {
		this.id = id;
	}

	public String get() {
		return id;
	}
}
