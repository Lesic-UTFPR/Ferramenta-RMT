package br.com.detection.domain.files;

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
