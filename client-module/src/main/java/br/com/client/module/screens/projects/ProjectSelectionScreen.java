package br.com.client.module.screens.projects;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;

import br.com.client.module.qualityAttributes.QualityAttribute;
import br.com.client.module.services.Identity;
import br.com.client.module.services.IntermediaryRepositoryImpl;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

@FXMLController("/br/com/client/module/projects/archprome-import-projetos.fxml")
public class ProjectSelectionScreen {

	private @ActionHandler FlowActionHandler actionHandler;

	private @FXMLViewFlowContext ViewFlowContext context;

	private @FXML BorderPane mainPane;

	private @FXML ProgressIndicator loading;

	private @FXML MenuItem importProjectBtn;

	private @FXML MenuItem applyCandidadesBtn;

	private @FXML MenuItem cleanCandidatesBtn;

	private @FXML TableView<RefactoringCandidateSelector> candidatesTable;

	private @FXML TableColumn<RefactoringCandidateSelector, Boolean> addCandidateColumn;

	private @FXML TableColumn<RefactoringCandidateSelector, String> classColumn;

	private @FXML TableColumn<RefactoringCandidateSelector, String> patternColumn;

	private @FXML TableColumn<RefactoringCandidateSelector, String> maintainabilityColumn;

	private @FXML TableColumn<RefactoringCandidateSelector, String> reliabilityColumn;

	private @FXML TableColumn<RefactoringCandidateSelector, String> reusabilityColumn;

	private final IntermediaryRepositoryImpl repository = new IntermediaryRepositoryImpl();

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	@PostConstruct
	public void init() {
		final Stage stage = context.getApplicationContext().getRegisteredObject(Stage.class);

		this.mainPane.prefWidthProperty().bind(stage.widthProperty());
		this.mainPane.prefHeightProperty().bind(stage.heightProperty().subtract(25d));

		this.importProjectBtn.setOnAction(a -> {
			try {
				this.searchProject();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		this.applyCandidadesBtn.setOnAction(a -> this.applyCandidates());
		this.cleanCandidatesBtn.setOnAction(a -> this.clearCandidates());

		this.initCandidatesTable();
	}

	private void applyCandidates() {
		final List<RefactoringCandidadeDTO> selected = this.candidatesTable.getItems().stream()
				.filter(c -> c.isSelected().get()).map(RefactoringCandidateSelector::getDto)
				.collect(Collectors.toList());

		if (selected.isEmpty()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Atenção");
			alert.setContentText("Ao menos um candidato a refatoração deve ser selecionado");
			alert.showAndWait();
		} else {
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			final File file = directoryChooser.showDialog(mainPane.getScene().getWindow());

			if (file != null) {
				this.downloadRefactoredProject(file.toPath(), selected);
				this.clearCandidates();
			}
		}

	}

	private void downloadRefactoredProject(Path file, List<RefactoringCandidadeDTO> selected) {

		if (!Files.isDirectory(file)) {
			return;
		}

		final Path refactoredProject = Paths.get(file.toFile().getAbsolutePath(),
				"refactored" + UUID.randomUUID() + ".zip");

		try (final InputStream is = new ByteArrayInputStream(this.repository.refactor(Identity.getID(), selected));
				final FileOutputStream fos = new FileOutputStream(refactoredProject.toFile())) {
			IOUtils.copy(is, fos);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initCandidatesTable() {
		this.candidatesTable.prefWidthProperty().bind(this.mainPane.widthProperty().multiply(0.997d));
		this.candidatesTable.prefHeightProperty().bind(this.mainPane.heightProperty().multiply(0.75d));

		this.addCandidateColumn.setCellValueFactory(c -> c.getValue().isSelected());
		this.addCandidateColumn.setCellFactory(CheckBoxTableCell.forTableColumn(addCandidateColumn));

		this.classColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassName()));
		this.patternColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEligiblePattern().name()));

		final NumberFormat nf = NumberFormat.getInstance();

		nf.setRoundingMode(RoundingMode.HALF_EVEN);
		nf.setMaximumFractionDigits(2);

		final Function<BigDecimal, String> formatParser = (v) -> {
			return v == null ? "" : nf.format(v);
		};

		final BiFunction<RefactoringCandidateSelector, QualityAttribute, SimpleStringProperty> qualityAttributePropertyCreator = (
				rc, qa) -> new SimpleStringProperty(
						formatParser.apply(rc.getDto().getEvaluation().getQualityAttributeValueByName(qa.name())));

		this.maintainabilityColumn.setCellValueFactory(
				c -> qualityAttributePropertyCreator.apply(c.getValue(), QualityAttribute.MAINTAINABILITY));
		this.reliabilityColumn.setCellValueFactory(
				c -> qualityAttributePropertyCreator.apply(c.getValue(), QualityAttribute.RELIABILITY));
		this.reusabilityColumn.setCellValueFactory(
				c -> qualityAttributePropertyCreator.apply(c.getValue(), QualityAttribute.REUSABILITY));

		candidatesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		final MenuItem showDetails = new MenuItem("Mostrar Detalhes");
		showDetails.setOnAction(a -> this.showDetailsAction());

		candidatesTable.setContextMenu(new ContextMenu(showDetails));
	}

	private void showDetailsAction() {
		final RefactoringCandidateSelector candidate = candidatesTable.selectionModelProperty().get().getSelectedItems()
				.stream().findFirst().get();

		Platform.runLater(() -> {
			try {
				CandidateDetailsDialog.open(candidate.getDto());
			} catch (FlowException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void searchProject() throws InterruptedException, ExecutionException {

		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File file = directoryChooser.showDialog(mainPane.getScene().getWindow());
		if (file != null) {

			executor.execute(() -> {
				try {
					executor.execute(() -> Platform.runLater(() -> this.loading.setVisible(true)));

					List<RefactoringCandidadeDTO> candidates = this.searchForCandidates(file);
					this.candidatesTable.setItems(FXCollections.observableArrayList(
							candidates.stream().map(RefactoringCandidateSelector::new).collect(Collectors.toList())));
				} finally {
					executor.execute(() -> Platform.runLater(() -> this.loading.setVisible(false)));
				}
			});

		}
	}

	private List<RefactoringCandidadeDTO> searchForCandidates(File file) {
		try {
			Identity.setID(repository.register(Identity.getName(), file.toPath()));

			return repository.evaluate(Identity.getID());
		} catch (Exception e) {
			e.printStackTrace();

			return Collections.emptyList();
		}
	}

	private void clearCandidates() {
		this.candidatesTable.setItems(FXCollections.emptyObservableList());
	}

}
