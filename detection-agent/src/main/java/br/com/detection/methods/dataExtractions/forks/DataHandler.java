package br.com.detection.methods.dataExtractions.forks;

import java.nio.file.Path;
import java.util.Collection;

public interface DataHandler {

	/**
	 * Parse the files to return it.
	 * @return All the files parsed.
	 */
	Collection<Object> getParsedFiles();

	/**
	 * Get a file by the parsedEntity.
	 * @return return a file.
	 */
	Collection<Path> getFiles();

	Object parseFile(Path file);

	Path getFile(Object parsedEntity);

	/**
	 * Get file from the bufferOfFiles by name.
	 * @param name File name.
	 * @return Return a file if it exists or else return null.
	 */
	Object getParsedFileByName(String name);

}
