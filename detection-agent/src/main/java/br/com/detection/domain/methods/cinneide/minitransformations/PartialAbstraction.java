package br.com.detection.domain.methods.cinneide.minitransformations;

import java.nio.file.Path;

import java.util.Collection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import br.com.detection.methods.dataExtractions.forks.DataHandler;

public class PartialAbstraction extends MinitransformationUitls {

	public void makePartialAbstraction(String clazz, DataHandler dataHandler) {
		final CompilationUnit cu = new CompilationUnit();
		final CompilationUnit baseCu = (CompilationUnit) dataHandler.getParsedFileByName(clazz);
		final Path file = dataHandler.getFile(baseCu);
		String newClassName = clazz;
		final String abstractClassName = String.format("Abstract%s", newClassName);
		ClassOrInterfaceDeclaration newClass = cu.addClass(abstractClassName);
		newClass.setAbstract(true);

		baseCu.findAll(MethodDeclaration.class).forEach(m -> {
			if (m.isAbstract()) {
				newClass.addMethod(m.getNameAsString()).setAbstract(true);
			}
		});

		baseCu.findAll(MethodDeclaration.class).forEach(m -> {
			if (!m.isAbstract()) {
				newClass.addMethod(m.getNameAsString()).setBody(m.getBody().get());
				m.remove();
			}
		});

		baseCu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> c.addExtendedType(newClass.getNameAsString()));

		this.writeChanges(cu, file.getParent().resolve(String.format("%s.java", abstractClassName)));
		this.writeChanges(baseCu, file);
	}

}
