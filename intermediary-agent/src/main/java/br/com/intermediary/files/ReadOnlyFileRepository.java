package br.com.intermediary.files;

import java.util.Optional;

import br.com.intermediary.files.collections.FileRepositoryCollections;
import br.com.messages.files.FileEntity;

public interface ReadOnlyFileRepository<T extends FileEntity> {

	Optional<T> get(FileRepositoryCollections collection, String id);

}
