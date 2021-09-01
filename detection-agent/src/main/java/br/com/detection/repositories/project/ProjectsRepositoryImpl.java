package br.com.detection.repositories.project;

import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.mongodb.morphia.Datastore;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import br.com.detection.domain.files.FileRepositoryCollections;
import br.com.messages.projects.Project;

@Stateless
public class ProjectsRepositoryImpl implements ProjectsRepository {

	private static final long serialVersionUID = 1L;

	private final String ORIGINAL_PROJECT_META = "originalProjectId";

	private final String SOURCE_FILE_NAME_META = "sourceFileName";

	private @Inject Datastore datastore;

	@Override
	public Optional<Project> get(FileRepositoryCollections collection, String id) {
		return Optional.ofNullable(this.getFilestore(collection).findOne(id)).map(this::toProject);
	}

	@Override
	public void put(FileRepositoryCollections collection, Project project) {
		this.fromDataFile(collection, project).save();
	}

	@Override
	public void remove(FileRepositoryCollections collection, Project project) {
		this.getFilestore(collection).remove(project.getId());
	}

	private GridFSInputFile fromDataFile(FileRepositoryCollections collection, Project project) {
		final GridFS fileStorage = this.getFilestore(collection);
		final GridFSInputFile file = fileStorage.createFile(project.getContent());
		file.setId(project.getId());
		file.setContentType(project.getContentType());
		file.setFilename(project.getId());
		file.setMetaData(new BasicDBObject());
		file.getMetaData().put(SOURCE_FILE_NAME_META, project.getName());
		
		Optional.ofNullable(project.getOriginalId()).ifPresent(oid -> file.getMetaData().put(ORIGINAL_PROJECT_META, oid));
		
		return file;
	}

	private Project toProject(GridFSDBFile file) {
		return new Project(file.getFilename(), file.getMetaData().get(SOURCE_FILE_NAME_META).toString(),
				() -> file.getInputStream(), file.getContentType());
	}

	private GridFS getFilestore(FileRepositoryCollections collection) {
		try {
			return new GridFS(this.datastore.getDB(), collection.get());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
