package br.com.detection.domain.methods.cinneide.minitransformations;

import java.nio.file.Path;
import java.util.Collection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.thoughtworks.proxy.kit.ObjectReference;

import br.com.detection.methods.dataExtractions.forks.DataHandler;

public class AbstractAccess extends MinitransformationUitls {

	public void makeAbstractAccess(String clazz, ObjectCreationExpr concrete, String inf, DataHandler dataHandler,
			Collection<String> skipMethods) {
		final CompilationUnit cu = new CompilationUnit();
		final CompilationUnit baseCu = (CompilationUnit) dataHandler.getParsedFileByName(clazz);
		final Path file = dataHandler.getFile(baseCu);
		final ClassOrInterfaceDeclaration newInterface = cu.addClass(clazz);

		baseCu.findAll(FieldDeclaration.class).forEach(o -> {
			for (String skipMethod : skipMethods) {
				if (o.getElementType().toString().equals(concrete.getTypeAsString()) && !o.getElementType().toString().equals(skipMethod)) {
					o.getVariables().forEach(v -> v.setType(inf));
				}
			}
		});
		
		this.writeChanges(baseCu, file);
	}
}
