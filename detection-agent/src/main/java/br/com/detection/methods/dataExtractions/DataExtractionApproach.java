package br.com.detection.methods.dataExtractions;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface DataExtractionApproach {
	/**
	 * Uses the parseSingle to parse many files.
	 * @param Many Files.
	 * @return A collection of Optional Objects.
	 */
	Collection<Object> parseAll(Path... files);
	
	/**
	 * Uses the JavaParser to parse a single java class.
	 * Transform the class into a AST (Abstract Syntax Tree).
	 * @param A Single File.
	 * @return A Optional object with a AST.
	 */
	Optional<Object> parseSingle(Path file);
	
}
