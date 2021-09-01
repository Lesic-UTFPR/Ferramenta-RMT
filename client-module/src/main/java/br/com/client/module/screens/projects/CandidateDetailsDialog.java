package br.com.client.module.screens.projects;

import javax.annotation.PostConstruct;

import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

@FXMLController(value = "/br/com/client/module/projects/candidate-details-dialog.fxml")
public class CandidateDetailsDialog {

	private @ActionHandler FlowActionHandler actionHandler;

	private @FXMLViewFlowContext ViewFlowContext context;

	private @FXML Label classLabel;

	private @FXML Label packageLabel;

	private @FXML Label patternLabel;

	private @FXML Label referenceLabel;

	private RefactoringCandidadeDTO candidate;

	@PostConstruct
	public void init() {
		this.candidate = this.context.getRegisteredObject(RefactoringCandidadeDTO.class);

		this.classLabel.setText("Classe: " + candidate.getClassName());
		this.packageLabel.setText("Pacote: " + candidate.getPkg());
		this.patternLabel.setText("Padrão Sugerido: " + candidate.getEligiblePattern().name());
		this.referenceLabel.setText("Referência: " + candidate.getReference().getTitle());

	}

	public static void open(RefactoringCandidadeDTO dto) throws FlowException {
		final Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.APPLICATION_MODAL);

		final FlowHandler flow = new Flow(CandidateDetailsDialog.class).createHandler();
		flow.getFlowContext().register(dto);
		flow.getFlowContext().register(dialog);

		StackPane pane = flow.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.ZOOM_IN));

		dialog.setScene(new Scene(pane));
		dialog.setTitle("Candidato");
		dialog.sizeToScene();
		dialog.showAndWait();
	}

}
