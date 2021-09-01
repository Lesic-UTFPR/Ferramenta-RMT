package br.com.detection.methods.dataExtractions.forks;

import static java.util.stream.Collectors.toList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.resource.spi.IllegalStateException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.zeroturnaround.zip.ZipUtil;

import com.github.javaparser.ast.CompilationUnit;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.files.FileRepositoryCollections;
import br.com.detection.methods.dataExtractions.AbstractSyntaxTree;
import br.com.detection.repositories.project.ProjectsRepository;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;

@Stateless
public class AbstractSyntaxTreeFork implements DataExtractionFork {

	private final ProjectsRepository projectsRepository;

	private final AbstractSyntaxTree ast = new AbstractSyntaxTree();

	private final Collection<AbstractSyntaxTreeDependent> dependentMethods;

	private final Map<Path, CompilationUnit> bufferOfFiles = new HashMap<>();

	private final AstHandler astHandler = new AstHandler();

	public AbstractSyntaxTreeFork() {
		this.projectsRepository = null;
		this.dependentMethods = null;
	}

	@Inject
	public AbstractSyntaxTreeFork(ProjectsRepository projectsRepository,
			@Any Instance<AbstractSyntaxTreeDependent> dependentMethods) {
		this.projectsRepository = projectsRepository;
		this.dependentMethods = StreamSupport.stream(dependentMethods.spliterator(), false).collect(toList());
	}

	@Override
	public Collection<RefactoringCandidate> findCandidates(Project project) {

		try {
			updatedBuffer(project);
			return this.dependentMethods.stream().flatMap(m -> m.extractCandidates(this).stream())
					.collect(Collectors.toList());
		} finally {
			clearBuffer();
		}
	}

	@Override
	public String refactor(Project project, RefactoringCandidate candidate) {

		try {
			updatedBuffer(project);

			final Optional<AbstractSyntaxTreeDependent> dependent = this.dependentMethods.stream()
					.filter(astDep -> astDep.toReference().equals(candidate.getReference())).findFirst();

			if (dependent.isPresent()) {
				dependent.get().refactor(this, candidate);

				try {
					return this.saveProject(project);
				} catch (IOException | IllegalStateException e) {
					throw new RuntimeException(e);
				}
			}

			throw new RuntimeException(String.format("Referência não encontrada para refatoração (%s).",
					ToStringBuilder.reflectionToString(candidate.getReference())));
		} catch (Exception e) {
			try {
				this.removeFile(Paths.get(project.getId()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		} finally {
			clearBuffer();
		}
	}

	private String saveProject(Project original) throws IOException, IllegalStateException {
		final String id = UUID.randomUUID().toString();
		final Path sourceCodeDir = Paths.get(original.getId());
		final Path compressedFile = Paths.get("refactored-".concat(id));

		if (!Files.exists(sourceCodeDir) || !Files.isDirectory(sourceCodeDir)) {
			throw new IllegalStateException();
		}

		if (Files.exists(compressedFile)) {
			org.apache.commons.io.FileUtils.forceDelete(compressedFile.toFile());
		}
		Files.createFile(compressedFile);

		ZipUtil.pack(sourceCodeDir.toFile(), compressedFile.toFile());

		try (FileInputStream fis = new FileInputStream(compressedFile.toFile())) {
			final Project p = new Project(id, "Refactored", () -> fis, "application/octet-stream", original.getId());

			projectsRepository.put(FileRepositoryCollections.REFACTORED_PROJECTS, p);

			return p.getId();
		} finally {
			this.removeFile(compressedFile);
		}
	}

	@Override
	public AbstractSyntaxTree getExtractionApproach() {
		return ast;
	}

	@Override
	public Collection<Object> getParsedFiles() {
		return this.ast.parseAll(bufferOfFiles.keySet().toArray(new Path[bufferOfFiles.keySet().size()]));
	}

	@Override
	public Path getFile(Object parsedEntity) {
		return this.bufferOfFiles.keySet().stream().filter(
				k -> this.astHandler.unitsMatch((CompilationUnit) bufferOfFiles.get(k), (CompilationUnit) parsedEntity))
				.findFirst().orElseThrow(() -> new IllegalArgumentException());
	}

	@Override
	public Collection<Path> getFiles() {
		return this.bufferOfFiles.keySet();
	}

	@Override
	public Object parseFile(Path file) {
		return this.bufferOfFiles.get(file);
	}

	public Object getParsedFileByName(String name) {
		final Optional<Path> file = this.bufferOfFiles.keySet().stream()
				.filter(f -> f.toAbsolutePath().toString().endsWith(String.format("/%s.java", name))).findFirst();

		return file.isPresent() ? this.bufferOfFiles.get(file.get()) : null;
	}
	/**
	 * Delete a file.
	 * @param p File path
	 * @throws IOException
	 */
	private void removeFile(Path p) throws IOException {
		if (Files.exists(p)) {
			org.apache.commons.io.FileUtils.forceDelete(p.toFile());
		}
	}
	/**
	 * Gets a project and update the BufferFile.
	 * The bufferFile is a Map of Path and CompilationUnit.
	 * @param project
	 */
	private void updatedBuffer(Project project) {
		final Path tmp = Paths.get(project.getId());

		Collection<Path> files = new ArrayList<>();
		try (InputStream is = Optional.ofNullable(project.getStream())
				.orElseGet(() -> this.projectsRepository.get(FileRepositoryCollections.PROJECTS, project.getId())
						.orElseThrow(IllegalArgumentException::new).getStream())) {
			this.removeFile(tmp);
			Files.createFile(tmp);

			try (FileOutputStream fos = new FileOutputStream(tmp.toFile())) {
				IOUtils.copy(is, fos);
			}

			ZipUtil.explode(tmp.toFile());

			files = Files.find(tmp, 1000, (p, attr) -> {
				return p.getFileName().toString().endsWith(".java");
			}).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.bufferOfFiles.clear();

		for (Path file : files) {
			final Object parsedFile = this.ast.parseSingle(file).orElse(null);

			if (parsedFile != null) {
				bufferOfFiles.put(file, (CompilationUnit) parsedFile);
			}
		}

		System.out.println("Classes sob avaliação = " + bufferOfFiles.values().size());
	}
	/**
	 * Clear the BufferFile.
	 */
	private void clearBuffer() {
		this.bufferOfFiles.clear();
	}

	@Override
	public boolean belongsTo(Reference methodReference) {
		return this.dependentMethods.stream().map(AbstractSyntaxTreeDependent::toReference)
				.anyMatch(methodReference::equals);
	}

	@Override
	public Collection<Reference> getReferences() {
		return this.dependentMethods.stream().map(AbstractSyntaxTreeDependent::toReference)
				.collect(Collectors.toList());
	}

}
