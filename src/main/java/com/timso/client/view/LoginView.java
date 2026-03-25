package com.timso.client.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView {
    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView() {
        // System.out.println("Giao diện đăng nhập & đăng ký.");
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        usernameField = new TextField();
        root.getStyleClass().add("login-root");
        String css = this.getClass().getResource("/css/style.css").toExternalForm();
        root.getStylesheets().add(css);
        usernameField.setPromptText("Tên đăng nhập");
        passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");
        loginButton = new Button("Đăng nhập");
        root.getChildren().addAll(usernameField, passwordField, loginButton);

    }

    public VBox getRoot() {
        return root;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

}
