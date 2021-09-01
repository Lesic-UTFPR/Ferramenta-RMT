package br.com.detection.domain.methods.cinneide.executors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import br.com.detection.domain.dataExtractions.ast.utils.AstHandler;
import br.com.detection.domain.methods.cinneide.Cinneide2000SingletonCanditate;
import br.com.detection.domain.methods.cinneide.minitransformations.MinitransformationUitls;
import br.com.detection.domain.methods.cinneide.minitransformations.PartialAbstraction;
import br.com.detection.domain.methods.weiL.WeiEtAl2014FactoryCanditate;
import br.com.detection.methods.dataExtractions.forks.DataHandler;
import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.patterns.DesignPattern;

public class Cinneide2000SingletonExecutor implements Cinneide2000Executor {

	private final AstHandler astHandler = new AstHandler();
	private final PartialAbstraction pa = new PartialAbstraction();

	@Override
	public boolean isApplicable(RefactoringCandidate candidate) {
		return candidate instanceof Cinneide2000SingletonCanditate
				&& DesignPattern.SINGLETON.equals(candidate.getEligiblePattern());
	}

	@Override
	public void refactor(RefactoringCandidate candidate, DataHandler dataHandler) {
		try {
			final Cinneide2000SingletonCanditate cinneidCandidate = (Cinneide2000SingletonCanditate) candidate;

			final Collection<CompilationUnit> allClasses = pa.getParsedClasses(dataHandler);
			final CompilationUnit baseCu = pa.updateBaseCompilationUnit(allClasses, cinneidCandidate);
			final Collection<ObjectCreationExpr> objectCreationExprs = new ArrayList<>(
					getUnique(getInstance(baseCu, dataHandler)));
			final String typeName = cinneidCandidate.getClassDeclaration().getNameAsString();
			pa.makePartialAbstraction(typeName, dataHandler);

			final Path fileSon = dataHandler.getFile(baseCu);
			final Optional<ClassOrInterfaceDeclaration> sonClass = baseCu.findFirst(ClassOrInterfaceDeclaration.class);
			final Optional<ConstructorDeclaration> constructor = baseCu.findFirst(ConstructorDeclaration.class);

			if (constructor.isPresent()) {
				constructor.get().setPrivate(true);
			} else {
				sonClass.ifPresent(c -> c.addConstructor(Modifier.PROTECTED));
			}

			baseCu.findFirst(ClassOrInterfaceDeclaration.class)
					.ifPresent(c -> c.addField(candidate.getClassName(), "instance", Modifier.PRIVATE));

			sonClass.ifPresent(c -> {
				c.addMethod("getInstance").addModifier(Modifier.PUBLIC).addModifier(Modifier.STATIC)
						.setBody(createSingletonBlock());
			});

			pa.writeChanges(baseCu, fileSon);

			for (CompilationUnit changeCu : findClassesWith(allClasses, typeName)) {
				changeCu.findAll(ObjectCreationExpr.class).forEach(objectCreationExpr -> {
					if (objectCreationExpr.getTypeAsString().equals(typeName)) {
						final String objType = typeName;

						final Path fileParent = dataHandler.getFile(changeCu);

						changeCu.findAll(FieldDeclaration.class).forEach(f -> {
							if (f.getElementType().toString().equals(objType)) {
								f.getVariables()
										.forEach(v -> v.setInitializer(String.format("%s.getInstance()", objType)));
							}
						});

						pa.writeChanges(changeCu, fileParent);
					}
				});
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private static Collection<ObjectCreationExpr> getUnique(Collection<ObjectCreationExpr> instances) {
		HashSet<Type> FieldSet = new HashSet<>();
		List<ObjectCreationExpr> te = new ArrayList<>();
		for (ObjectCreationExpr instance : instances) {
			if (!FieldSet.add(instance.getType())) {
				te.add(instance);
			}
		}
		instances.removeAll(te);
		return instances;
	}

	private static Collection<ObjectCreationExpr> getInstance(CompilationUnit cu, DataHandler dataHandler) {
		List<ObjectCreationExpr> instance = new ArrayList<>();
		cu.findAll(ObjectCreationExpr.class).stream().forEach(i -> {
			Type element = i.getType();
			if (element.isReferenceType()) {
				if (dataHandler.getParsedFileByName(i.getType().toString()) != null
						&& !i.getType().toString().contains("Exception")) {
					instance.add(i);
				}
			}
		});
		return instance;
	}

	private Collection<CompilationUnit> findClassesWith(Collection<CompilationUnit> cu, String clazz) {
		HashSet<CompilationUnit> cuList = new HashSet<>();
		for (CompilationUnit compilationUnit : cu) {
			compilationUnit.findAll(ObjectCreationExpr.class).forEach(o -> {
				if (o.getTypeAsString().equals(clazz)) {
					cuList.add(compilationUnit);
				}
			});
		}
		return cuList;
	}

	private BlockStmt createSingletonBlock() {
		BlockStmt blockStmt = new BlockStmt();
		IfStmt ifStmt = new IfStmt();
		Expression expression = JavaParser.parseExpression("instance == null");

		ifStmt.setCondition(expression);
		ifStmt.setThenStmt(JavaParser.parseStatement("instance = new instance();"));

		blockStmt.addStatement(ifStmt);
		blockStmt.addStatement(JavaParser.parseStatement("return instance;"));

		return blockStmt;
	}

//	public static ArrayList<ObjectCreationExpr> getSame(ArrayList<ObjectCreationExpr> fields) {
//		HashSet<ObjectCreationExpr> set = new HashSet<>();
//		HashSet<ObjectCreationExpr> same = new HashSet<>();
//		for (ObjectCreationExpr field : fields) {
//			if (set.add(field) == false) {
//				same.add(field);
//			}
//		}
//		ArrayList<ObjectCreationExpr> f = new ArrayList<>(same);
//		return f;
//	}

}
