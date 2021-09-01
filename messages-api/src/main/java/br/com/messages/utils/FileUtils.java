package br.com.messages.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.activation.MimetypesFileTypeMap;

public class FileUtils {

	public static String getContentType(Path path) {
		return FileUtils.getContentType(Optional.ofNullable(path).map(Path::toFile).orElse(null));
	}

	public static String getContentType(File file) {
		final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

		return mimeTypesMap.getContentType(file);
	}

	public static Path createOrOverrite(String path) throws IOException {
		final Path file = Paths.get(path);

		if (Files.exists(file)) {
			Files.delete(file);
		}

		return Files.createFile(file);
	}

}
