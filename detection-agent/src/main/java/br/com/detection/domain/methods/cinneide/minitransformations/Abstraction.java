package br.com.detection.domain.methods.cinneide.minitransformations;

import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import br.com.detection.methods.dataExtractions.forks.DataHandler;

public class Abstraction extends MinitransformationUitls{

	public void makeAbstraction(String clazz, DataHandler dataHandler) {
		final CompilationUnit cu = new CompilationUnit();
		final String className = clazz;
		final CompilationUnit baseCu = (CompilationUnit) dataHandler.getParsedFileByName(className);
		final Path file = dataHandler.getFile(baseCu);
		final ClassOrInterfaceDeclaration newInterface = cu.addClass(className);
		final String newInterfaceName = String.format("%sInf", className);
		
		newInterface.setInterface(true);
		
		baseCu.findFirst(ClassOrInterfaceDeclaration.class).
		ifPresent(c -> c.addImplementedType(newInterfaceName));
		this.writeChanges(cu, file.getParent().resolve(String.format("%s.java",newInterfaceName)));
		this.writeChanges(baseCu, file);
		
	}
}
