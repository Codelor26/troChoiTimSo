package com.timso.client;

import com.timso.client.view.LoginView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    // phai extends Application de chay duoc JavaFX
    @Override
    public void start(Stage primaryStage) {
        // System.out.println("Chương trình trò chơi bắt đầu.");
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), 500, 500);
        primaryStage.setTitle("Codelor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
