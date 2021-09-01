package br.com.detection.domain.methods.cinneide.minitransformations;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.cinneide.Cinneide2000Candidate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;

public class MinitransformationUitls {

	private final AstHandler astHandler = new AstHandler();

	public String getClassDeclarationName(ObjectCreationExpr objectCreationExpr, DataHandler dataHandler) {

		final Optional<CompilationUnit> cu = Optional
				.ofNullable(dataHandler.getParsedFileByName(objectCreationExpr.getType().getNameAsString()))
				.map(o -> (CompilationUnit) o);

		final Optional<ClassOrInterfaceDeclaration> declaration = cu
				.map(c -> this.astHandler.getClassOrInterfaceDeclaration(c).orElse(null));

		final String className = declaration.map(ClassOrInterfaceDeclaration::getNameAsString)
				.orElseThrow(IllegalStateException::new);

		return className;
	}

	public CompilationUnit updateBaseCompilationUnit(Collection<CompilationUnit> allClasses,
			Cinneide2000Candidate candidate) {
		return allClasses.stream().filter(c -> {
			return this.astHandler.unitsMatch(c, Optional.of(candidate.getClassDeclaration()),
					Optional.of(candidate.getPackageDeclaration()));
		}).findFirst().get();
	}

	public Collection<CompilationUnit> getParsedClasses(DataHandler dataHandler) {
		return dataHandler.getParsedFiles().stream().map(CompilationUnit.class::cast).collect(Collectors.toList());
	}

	public void writeChanges(CompilationUnit cUnit, Path file) {
		try (FileWriter fileWriter = new FileWriter(file.toFile())) {
			fileWriter.write(cUnit.toString());
			fileWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public MethodDeclaration makeAbstract(ConstructorDeclaration c, String newName) {
		MethodDeclaration methodDeclaration = new MethodDeclaration();
		BlockStmt blockStmt = new BlockStmt();
		NodeList<Parameter> parameters = c.getParameters();

		blockStmt.addStatement(JavaParser.parseStatement(String.format("return new %s();", c.getNameAsString())));

		methodDeclaration.setName(newName);
		methodDeclaration.setType(c.getNameAsString());
		for (Parameter parameter : parameters) {
			methodDeclaration.addParameter(parameter);
		}
		methodDeclaration.setBody(blockStmt);
		
		return methodDeclaration;
	}
}
