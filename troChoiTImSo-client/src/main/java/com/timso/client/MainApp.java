package com.timso.client;

import com.timso.client.network.GameClient;
import com.timso.client.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameClient.getInstance();

        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView, 1280, 620);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Game Tìm Số");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}