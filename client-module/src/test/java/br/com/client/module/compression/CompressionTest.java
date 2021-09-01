package br.com.client.module.compression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.zeroturnaround.zip.ZipUtil;

public class CompressionTest {

	private final Path sourceDirPath = Paths
			.get("/home/luan/Desenvolvimento/Pessoal/Repositories/arquiteturamultiagentes/client-project/src");

	private final Path compressedFile = Paths.get("compressed.zip");

	private final Path decompressionFolder = Paths.get("decompressed_src");

	public void test() throws IOException {

		Files.deleteIfExists(compressedFile);
		Files.createFile(compressedFile);

		ZipUtil.pack(sourceDirPath.toFile(), compressedFile.toFile());

		ZipUtil.explode(compressedFile.toFile());

		compressedFile.toFile().renameTo(decompressionFolder.toFile());

	}

}
