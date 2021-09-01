package br.com.detection.domain.methods;

import java.util.List;
import java.util.Set;

import br.com.detection.domain.methods.details.Author;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public interface DetectionMethod {

	Set<DesignPattern> getDesignPatterns();

	String getTitle();

	int getYear();

	List<Author> getAuthors();

	Reference toReference();

}
