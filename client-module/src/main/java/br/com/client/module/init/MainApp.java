package br.com.client.module.init;

import br.com.client.module.screens.projects.ProjectSelectionScreen;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		final Flow flow = new Flow(ProjectSelectionScreen.class);

		final FlowHandler flowHandler = flow.createHandler();
		flowHandler.getFlowContext().getApplicationContext().register(this.getHostServices());
		flowHandler.getFlowContext().getApplicationContext().register(primaryStage);

		final StackPane pane = flowHandler
				.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.ZOOM_IN));
		pane.maxWidthProperty().bind(primaryStage.widthProperty());
		pane.maxHeightProperty().bind(primaryStage.heightProperty());
		primaryStage.setScene(new Scene(pane));
		primaryStage.setTitle("Client App - Painel Incial");
		primaryStage.setMaximized(true);
		primaryStage.show();

		primaryStage.setOnCloseRequest(t -> {
			Platform.exit();
			System.exit(0);
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

}
