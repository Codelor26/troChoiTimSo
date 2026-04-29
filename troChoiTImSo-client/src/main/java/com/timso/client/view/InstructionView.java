package com.timso.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InstructionView extends StackPane {

    public InstructionView(Runnable onCloseAction, Runnable onStartAction) {
        getStyleClass().add("rules-overlay");
        setVisible(false);
        setManaged(false);

        VBox panel = new VBox(18);
        panel.getStyleClass().add("rules-panel");
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(600);
        panel.setMaxHeight(450);

        LanguageManager lang = LanguageManager.getInstance();

        Label title = new Label(lang.getString("instruction.title"));
        title.getStyleClass().add("rules-title");

        VBox contentBox = new VBox(14);
        contentBox.getStyleClass().add("rules-content-box");
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(24, 28, 24, 28));

        Label heading = new Label(lang.getString("instruction.heading"));
        heading.getStyleClass().add("rules-heading");

        Label rule1 = new Label(lang.getString("instruction.rule1"));
        Label rule2 = new Label(lang.getString("instruction.rule2"));
        Label rule3 = new Label(lang.getString("instruction.rule3"));
        Label rule4 = new Label(lang.getString("instruction.rule4"));
        Label rule5 = new Label(lang.getString("instruction.rule5"));

        rule1.getStyleClass().add("rules-text");
        rule2.getStyleClass().add("rules-text");
        rule3.getStyleClass().add("rules-text");
        rule4.getStyleClass().add("rules-text");
        rule5.getStyleClass().add("rules-text");

        contentBox.getChildren().addAll(heading, rule1, rule2, rule3, rule4, rule5);

        Button btnClose = new Button(lang.getString("instruction.button.close"));
        btnClose.getStyleClass().addAll("action-button", "rules-close-button");
        btnClose.setOnAction(e -> {
            if (onCloseAction != null) {
                onCloseAction.run();
            }
        });

        Button btnStart = new Button(lang.getString("instruction.button.start"));
        btnStart.getStyleClass().addAll("action-button", "rules-start-button");
        btnStart.setOnAction(e -> {
            if (onStartAction != null) {
                onStartAction.run();
            }
        });

        HBox buttonRow = new HBox(16, btnClose, btnStart);
        buttonRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, contentBox, buttonRow);

        getChildren().add(panel);
        setAlignment(Pos.CENTER);

        setOnMouseClicked(e -> {
            if (onCloseAction != null) {
                onCloseAction.run();
            }
        });

        panel.setOnMouseClicked(e -> e.consume());
    }

    public void showOverlay() {
        setVisible(true);
        setManaged(true);
    }

    public void hideOverlay() {
        setVisible(false);
        setManaged(false);
    }

}
