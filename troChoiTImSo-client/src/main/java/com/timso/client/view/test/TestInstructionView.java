package com.timso.client.view.test;

import com.timso.client.view.InstructionView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestInstructionView extends Application {
    @Override
    public void start(Stage stage) {
        InstructionView view = new InstructionView(
                () -> System.out.println("Closed"),
                () -> System.out.println("Start game"));

        view.showOverlay();

        Scene scene = new Scene(view, 800, 600);

        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("Test InstructionView");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
