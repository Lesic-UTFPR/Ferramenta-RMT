package br.com.detection.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ClassParser {

	private final String DEFAULT_JAVA_SRC = "src/main/java";

	public Optional<Path> toPath(Class<?> clazz) throws MalformedURLException, FileNotFoundException {
		final Path basePath = Paths.get("");

		final String finalPath = Paths.get(basePath.toAbsolutePath().toUri().toURL().getPath(), DEFAULT_JAVA_SRC)
				.toString();

		final Path file = Paths
				.get(String.format("%s/%s.java", finalPath, clazz.getName().replaceAll("\\.", File.separator)));

		return Files.exists(file) ? Optional.of(file) : Optional.empty();
	}

}
