package br.com.client.module.screens;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CandidatesOptionsScreen extends Stage {

	public CandidatesOptionsScreen(Stage previous) {
		Label secondLabel = new Label("I'm a Label on new Window");

		StackPane secondaryLayout = new StackPane();
		secondaryLayout.getChildren().add(secondLabel);

		Scene scene = new Scene(secondaryLayout, 400, 600);

		this.setTitle("Second Stage");
		this.setScene(scene);
		this.setX(previous.getX());
		this.setY(previous.getY());

		this.show();
	}

}
