package br.com.intermediary.files;

import br.com.intermediary.files.collections.FileRepositoryCollections;
import br.com.messages.files.FileEntity;

public interface FileRepository<T extends FileEntity> extends ReadOnlyFileRepository<T> {

	void put(FileRepositoryCollections collection, T project);

	void remove(FileRepositoryCollections collection, T project);

}
