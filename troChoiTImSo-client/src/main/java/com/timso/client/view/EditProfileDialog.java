package com.timso.client.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import com.timso.client.network.AuthClient;
import com.timso.common.model.User;

public class EditProfileDialog {
    private final Stage stage;
    private final User currentUser;
    private Runnable onSuccess;

    private final TextField txtUsername = new TextField();
    private final PasswordField txtPassword = new PasswordField();
    private final TextField txtFullName = new TextField();
    private final DatePicker dpBirthDay = new DatePicker();
    private final RadioButton rbMale = new RadioButton("Male");
    private final RadioButton rbFemale = new RadioButton("Female");
    private final Label lblError = new Label("");

    public EditProfileDialog(User user) {
        this.currentUser = user;
        this.stage = new Stage();
        LanguageManager lang = LanguageManager.getInstance();
        stage.setTitle(lang.getString("edit.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        initializeUI(lang);
    }

    private void initializeUI(LanguageManager lang) {
        configureDatePicker();

        Label title = new Label(lang.getString("edit.title").toUpperCase());
        title.getStyleClass().add("form-title");

        txtUsername.setText(currentUser.getUserName() != null ? currentUser.getUserName() : "");
        txtUsername.setPromptText(lang.getString("edit.username.prompt"));

        txtFullName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        txtFullName.setPromptText(lang.getString("edit.fullname.prompt"));

        txtPassword.setPromptText(lang.getString("edit.password.prompt"));

        if (currentUser.getDob() != null) {
            dpBirthDay.setValue(currentUser.getDob().toLocalDate());
        }

        rbMale.setText(lang.getString("edit.gender.male"));
        rbFemale.setText(lang.getString("edit.gender.female"));
        boolean isMale = !"Female".equalsIgnoreCase(currentUser.getGender());
        rbMale.setSelected(isMale);
        rbFemale.setSelected(!isMale);

        ToggleGroup genderGroup = new ToggleGroup();
        rbMale.setToggleGroup(genderGroup);
        rbFemale.setToggleGroup(genderGroup);

        lblError.getStyleClass().add("error-label");
        lblError.setWrapText(true);
        lblError.setMaxWidth(520);
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnSave = new Button(lang.getString("edit.btn.save"));
        btnSave.getStyleClass().add("action-button");
        btnSave.setOnAction(e -> handleSave(lang));

        Button btnCancel = new Button(lang.getString("edit.btn.cancel"));
        btnCancel.getStyleClass().add("action-button");
        btnCancel.setOnAction(e -> stage.close());

        HBox actionRow = new HBox(20, btnCancel, btnSave);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.getStyleClass().add("action-row");

        HBox genderRow = new HBox(24, rbMale, rbFemale);
        genderRow.setAlignment(Pos.CENTER_LEFT);
        genderRow.getStyleClass().add("gender-row");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("register-grid");
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createFieldBox(lang.getString("edit.label.fullname"), txtFullName), 0, 0);
        grid.add(createFieldBox(lang.getString("edit.label.birthday"), dpBirthDay), 1, 0);
        grid.add(createFieldBox(lang.getString("edit.label.username"), txtUsername), 0, 1);
        grid.add(createFieldBox(lang.getString("edit.label.password"), txtPassword), 1, 1);
        grid.add(genderRow, 0, 2);
        grid.add(actionRow, 1, 2);

        VBox card = new VBox(22);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().addAll("form-card", "register-card");
        card.getChildren().addAll(title, grid, lblError);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("screen-panel");

        Scene scene = new Scene(root, 660, 400);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
    }

    private VBox createFieldBox(String labelText, Control input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        input.getStyleClass().add("input-field");
        input.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(6, label, input);
        box.getStyleClass().add("field-group");
        return box;
    }

    private void configureDatePicker() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpBirthDay.setPromptText("dd/MM/yyyy");
        dpBirthDay.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, formatter);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    private void handleSave(LanguageManager lang) {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblError.setStyle("-fx-text-fill: #d94b4b;");

        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String fullName = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
        String password = txtPassword.getText() == null ? "" : txtPassword.getText().trim();
        LocalDate birthDay = dpBirthDay.getValue();
        String gender = rbMale.isSelected() ? "Male" : "Female";

        if (fullName.isEmpty()) {
            showError(lang.getString("edit.err.fullname.empty"));
            return;
        }

        if (!fullName.matches("^[\\p{L}\\s]+$")) {
            showError(lang.getString("edit.err.fullname.format"));
            return;
        }

        if (birthDay == null) {
            showError(lang.getString("edit.err.birthday.empty"));
            return;
        }

        if (birthDay.isAfter(LocalDate.now())) {
            showError(lang.getString("edit.err.birthday.future"));
            return;
        }

        AuthClient authClient = new AuthClient();
        boolean success = authClient.updateUserInfo(
                currentUser.getID(),
                username,
                fullName,
                gender,
                birthDay.toString(),
                password.isEmpty() ? "" : password);

        if (!success) {
            showError(authClient.getLastError() == null ? "Cập nhật thất bại." : authClient.getLastError());
            return;
        }

        currentUser.setUsername(username);
        currentUser.setFullname(fullName);
        currentUser.setGender(gender);
        currentUser.setDob(java.sql.Date.valueOf(birthDay));

        PlayerSession.setCurrentUser(currentUser);

        lblError.setText(lang.getString("edit.success"));
        lblError.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: 700;");
        lblError.setVisible(true);
        lblError.setManaged(true);

        if (onSuccess != null) {
            onSuccess.run();
        }

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(stage::close);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: #d94b4b; -fx-font-weight: 700;");
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void show() {
        stage.showAndWait();
    }
}
