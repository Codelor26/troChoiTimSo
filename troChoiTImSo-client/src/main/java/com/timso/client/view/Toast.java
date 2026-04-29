package com.timso.client.view;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Toast {
    public static void show(Node owner, String message, int durationMs) {
        StackPane[] rootPane = new StackPane[1];
        Node current = owner;
        while (current.getParent() != null) {
            current = current.getParent();
            if (current instanceof StackPane) {
                rootPane[0] = (StackPane) current;
                break;
            }
        }

        if (rootPane[0] == null)
            return;

        Label toastLabel = new Label(message);
        toastLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.8);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 20 10 20;" +
                        "-fx-background-radius: 30;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;");
        toastLabel.setOpacity(0);

        StackPane.setAlignment(toastLabel, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(toastLabel, new javafx.geometry.Insets(0, 0, 50, 0));
        rootPane[0].getChildren().add(toastLabel);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(durationMs));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootPane[0].getChildren().remove(toastLabel));

        SequentialTransition seq = new SequentialTransition(toastLabel, fadeIn, pause, fadeOut);
        seq.play();
    }
}