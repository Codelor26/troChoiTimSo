package com.timso.client.view.test;

import com.timso.client.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestGameView extends Application {

    @Override
    public void start(Stage stage) {
        try {
            GameView view = new GameView(
                    "Player 1",
                    "E:\\laptrinhungdungmang\\troChoiTImSo\\src\\main\\resources\\icon\\Diversity-Avatars-Avatars-Charlie-chaplin.512.png" // <--
                                                                                                                                           // đổi
                                                                                                                                           // đúng
                                                                                                                                           // file
                                                                                                                                           // của
                                                                                                                                           // bạn
            );

            Scene scene = new Scene(view, 900, 600);

            var css = getClass().getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.out.println("⚠️ Không tìm thấy CSS");
            }

            stage.setTitle("Test GameView");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}