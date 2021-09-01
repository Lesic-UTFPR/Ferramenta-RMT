package br.com.detection.methods.dataExtractions;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;

public class AbstractSyntaxTree implements DataExtractionApproach {

	@Override
	public Collection<Object> parseAll(Path... files) {
		return Stream.of(files).map(this::parseSingle).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Object> parseSingle(Path file) {
		try (final FileInputStream fis = new FileInputStream(file.toFile())) {
			return Optional.of(JavaParser.parse(fis));
		} catch (Exception e) {
			System.err.println("---------------------------------------------------------------");
			System.err.println("Failed to parse file " + file == null ? "null" : file.toFile().getName());
			e.printStackTrace();
			System.err.println("Failed to parse file " + file == null ? "null" : file.toFile().getName());
			System.err.println("---------------------------------------------------------------");
			return Optional.empty();
		}
	}

}
