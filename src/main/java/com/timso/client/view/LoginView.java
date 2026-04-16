// client - 3
package com.timso.client.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;

import com.timso.common.model.User;
import com.timso.server.dao.*;

public class LoginView extends StackPane {

    private static final Pattern FULLNAME_PATTERN = Pattern.compile("^[\\p{L}\\s]+$");
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9._%+-]*@gmail\\.com$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern
            .compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    private final StackPane loginPage;
    private final StackPane registerPage;

    private final TextField txtUsername = new TextField();
    private final PasswordField txtPassword = new PasswordField();

    private final TextField txtFullName = new TextField();
    private final DatePicker dpBirthDay = new DatePicker();
    private final TextField txtEmail = new TextField();
    private final PasswordField txtRegisterPassword = new PasswordField();

    private final Label lblUsernameError = new Label("");
    private final Label lblPasswordError = new Label("");
    private final Label lblFullNameError = new Label("");
    private final Label lblBirthDayError = new Label("");
    private final Label lblEmailError = new Label("");
    private final Label lblRegisterPasswordError = new Label("");

    private final RadioButton rbMale = new RadioButton("male");
    private final RadioButton rbFemale = new RadioButton("female");

    private final Button btnOpenRegister = new Button("Register");
    private final Button btnLogin = new Button("Login");
    private final Button btnBack = new Button("Back");
    private final Button btnCreate = new Button("Create");

    private InstructionView instructionView;

    public LoginView() {
        getStyleClass().add("auth-root");

        getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        // Objects.requireNonNull(getClass().getResource("/css/reset-dialog.css")).toExternalForm());

        configureDatePicker();

        lblUsernameError.getStyleClass().add("error-label");
        lblPasswordError.getStyleClass().add("error-label");
        lblFullNameError.getStyleClass().add("error-label");
        lblBirthDayError.getStyleClass().add("error-label");
        lblEmailError.getStyleClass().add("error-label");
        lblRegisterPasswordError.getStyleClass().add("error-label");

        loginPage = createScreen(buildLoginContent());
        registerPage = createScreen(buildRegisterContent());

        getChildren().addAll(loginPage, registerPage);
        showLoginPage();

        btnLogin.setOnAction(e -> validateLoginForm());
        btnCreate.setOnAction(e -> validateRegisterForm());

        btnOpenRegister.setOnAction(e -> showRegisterPage());
        btnBack.setOnAction(e -> showLoginPage());

        txtUsername.textProperty().addListener((obs, oldVal, newVal) -> clearFieldError(txtUsername, lblUsernameError));
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> clearFieldError(txtPassword, lblPasswordError));

        txtFullName.textProperty().addListener((obs, oldVal, newVal) -> clearFieldError(txtFullName, lblFullNameError));
        dpBirthDay.valueProperty().addListener((obs, oldVal, newVal) -> clearFieldError(dpBirthDay, lblBirthDayError));
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> clearFieldError(txtEmail, lblEmailError));
        txtRegisterPassword.textProperty()
                .addListener((obs, oldVal, newVal) -> clearFieldError(txtRegisterPassword, lblRegisterPasswordError));
    }

    private void configureDatePicker() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        dpBirthDay.setPromptText("dd/MM/yyyy");
        dpBirthDay.setEditable(false);

        dpBirthDay.setConverter(new StringConverter<>() {
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

        dpBirthDay.setOnMouseClicked(e -> dpBirthDay.show());
        dpBirthDay.getEditor().setOnMouseClicked(e -> dpBirthDay.show());
    }

    private StackPane createScreen(Node content) {
        StackPane screen = new StackPane(content);
        screen.getStyleClass().add("screen-panel");
        screen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        screen.prefWidthProperty().bind(widthProperty());
        screen.prefHeightProperty().bind(heightProperty());
        return screen;
    }

    private Node buildLoginContent() {
        HBox wrapper = new HBox(90);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.getStyleClass().add("page-wrapper");

        VBox brandBox = new VBox(30);
        brandBox.setAlignment(Pos.CENTER);
        brandBox.getStyleClass().add("brand-box");

        Label slogan = new Label("Find numbers from 1 to 100");
        slogan.getStyleClass().add("brand-text");

        brandBox.getChildren().addAll(createBrandIcon(), slogan);

        VBox card = new VBox(12);
        // card.setAlignment(Pos.TOP_CENTER);
        card.setAlignment(Pos.CENTER);
        card.setMaxHeight(350);
        card.setMaxWidth(350);
        card.setSpacing(10);
        card.getStyleClass().addAll("form-card", "login-card");

        Label title = new Label("LOGIN");
        title.getStyleClass().add("form-title");

        btnOpenRegister.getStyleClass().add("action-button");
        btnLogin.getStyleClass().add("action-button");

        HBox buttonRow = new HBox(40, btnOpenRegister, btnLogin);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.getStyleClass().add("action-row");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox();
        Label label = new Label("Password");
        label.getStyleClass().add("form-label");

        Button forgotBtn = new Button("Forgot Password?");
        forgotBtn.getStyleClass().add("link-button");
        forgotBtn.setOnAction(e -> openResetDialog());

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        header.getChildren().addAll(label, spacerHeader, forgotBtn);

        VBox passwordBox = new VBox(4, header, txtPassword, lblPasswordError);
        passwordBox.getStyleClass().add("field-group");
        txtPassword.getStyleClass().add("input-field");

        card.getChildren().addAll(
                title,
                createFieldBox("Username", txtUsername, lblUsernameError),
                passwordBox,
                spacer,
                buttonRow);

        wrapper.getChildren().addAll(brandBox, card);
        return wrapper;
    }

    private Node buildRegisterContent() {
        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);

        VBox card = new VBox(22);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().addAll("form-card", "register-card");

        Label title = new Label("REGISTER");
        title.getStyleClass().add("form-title");

        ToggleGroup genderGroup = new ToggleGroup();
        rbMale.setToggleGroup(genderGroup);
        rbFemale.setToggleGroup(genderGroup);
        rbMale.setSelected(true);

        HBox genderRow = new HBox(28, rbMale, rbFemale);
        genderRow.setAlignment(Pos.CENTER_LEFT);
        genderRow.getStyleClass().add("gender-row");

        btnBack.getStyleClass().add("action-button");
        btnCreate.getStyleClass().add("action-button");

        HBox actionRow = new HBox(28, btnBack, btnCreate);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.getStyleClass().add("action-row");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("register-grid");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(createFieldBox("Fullname", txtFullName, lblFullNameError), 0, 0);
        grid.add(createFieldBox("Birth day", dpBirthDay, lblBirthDayError), 1, 0);
        // GridPane.setHgrow(dpBirthDay, Priority.ALWAYS);
        // dpBirthDay.setMaxWidth(Double.MAX_VALUE);
        grid.add(createFieldBox("Email", txtEmail, lblEmailError), 0, 1);
        grid.add(createFieldBox("Password", txtRegisterPassword, lblRegisterPasswordError), 1, 1);
        grid.add(genderRow, 0, 2);
        grid.add(actionRow, 1, 2);

        card.getChildren().addAll(title, grid);
        wrapper.getChildren().add(card);

        return wrapper;
    }

    private VBox createFieldBox(String labelText, Control input, Label errorLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        input.getStyleClass().add("input-field");
        input.setMaxWidth(Double.MAX_VALUE);

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        VBox box = new VBox(4, label, input, errorLabel);
        box.getStyleClass().add("field-group");
        return box;
    }

    private ImageView createBrandIcon() {
        String iconPath = Objects.requireNonNull(
                getClass().getResource("/icon/Custom-Icon-Design-Pretty-Office-9-Magnifying-glass.256.png"))
                .toExternalForm();

        Image icon = new Image(iconPath);
        ImageView iconView = new ImageView(icon);

        iconView.setFitWidth(220);
        iconView.setFitHeight(220);
        iconView.setPreserveRatio(true);
        iconView.setSmooth(true);

        return iconView;
    }

    private void validateLoginForm() {
        clearFieldError(txtUsername, lblUsernameError);
        clearFieldError(txtPassword, lblPasswordError);

        String username = safeTrim(txtUsername.getText());
        String password = safeTrim(txtPassword.getText());

        if (username.isEmpty()) {
            showFieldError(txtUsername, lblUsernameError, "Please enter username");
            return;
        } else if (password.isEmpty()) {
            showFieldError(txtPassword, lblPasswordError, "Please enter password");
            return;
        } else {
            UserDAO userDao = new UserDAO();
            User user = userDao.login(username, password);
            if (user != null) {
                System.out.println("Login successful! Welcome, " + user.getFullName());
                handleAfterLogin(user);
            } else {
                showFieldError(txtPassword, lblPasswordError, "Invalid username or password");
                return;
            }
        }
        // System.out.println("Bat dau dang nhap...");
    }

    private void handleAfterLogin(User user) {
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            getScene().setRoot(new ProfileDialog(user)); // truyền user
        } else {
            getScene().setRoot(new HomeView(
                    user.getPlayerName(),
                    user.getAvatar()));
        }
    }

    private void validateRegisterForm() {
        clearFieldError(txtFullName, lblFullNameError);
        clearFieldError(dpBirthDay, lblBirthDayError);
        clearFieldError(txtEmail, lblEmailError);
        clearFieldError(txtRegisterPassword, lblRegisterPasswordError);

        String fullName = safeTrim(txtFullName.getText());
        LocalDate birthDay = dpBirthDay.getValue();
        String email = safeTrim(txtEmail.getText());
        String password = safeTrim(txtRegisterPassword.getText());

        if (fullName.isEmpty()) {
            showFieldError(txtFullName, lblFullNameError, "Please enter full name");
            return;
        }

        if (!FULLNAME_PATTERN.matcher(fullName).matches()) {
            showFieldError(txtFullName, lblFullNameError, "Full name must contain letters only");
            return;
        }

        if (birthDay == null) {
            showFieldError(dpBirthDay, lblBirthDayError, "Please choose birth day");
            return;
        }

        if (birthDay.isAfter(LocalDate.now())) {
            showFieldError(dpBirthDay, lblBirthDayError, "Birth day cannot be in the future");
            return;
        }

        if (email.isEmpty()) {
            showFieldError(txtEmail, lblEmailError, "Please enter email");
            return;
        }

        if (!GMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(txtEmail, lblEmailError, "Email must end with @gmail.com && start with letter");
            return;
        }

        if (password.isEmpty()) {
            showFieldError(txtRegisterPassword, lblRegisterPasswordError, "Please enter password");
            return;
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            showFieldError(
                    txtRegisterPassword,
                    lblRegisterPasswordError,
                    "Min 8 chars, include letter, number, special char");
            return;
        }

        System.out.println("Bat dau dang ky tài khoản...");
        User newUser = new User();
        newUser.setUsername(email);
        newUser.setPassword(password);
        newUser.setFullname(fullName);
        newUser.setGender(rbMale.isSelected() ? "Male" : "Female");
        newUser.setDob(java.sql.Date.valueOf(birthDay));

        UserDAO userDao = new UserDAO();
        if (userDao.checkEmailExists(email)) {
            showFieldError(txtEmail, lblEmailError, "Email already exists");
            return;
        }
        if (userDao.register(newUser)) {
            System.out.println("Registration successful! You can now log in.");
            showSuccessDialog();
            showLoginPage();
        } else {
            showFieldError(txtEmail, lblEmailError, "Registration failed. Try a different email.");

        }
    }

    private void openResetDialog() {
        Stage stage = new Stage();
        stage.setTitle("Reset Password");

        VBox root = new VBox();
        root.getStyleClass().add("reset-popup-container");
        root.setPrefWidth(320);

        Label titleLabel = new Label("Secure Password Reset");
        titleLabel.getStyleClass().add("reset-header-text");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Enter your account email");
        txtEmail.getStyleClass().add("reset-input-field");

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("New password");
        txtNewPass.getStyleClass().add("reset-input-field");

        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Confirm new password");
        txtConfirm.getStyleClass().add("reset-input-field");

        Label lblError = new Label();
        lblError.getStyleClass().add("reset-error-message");

        Button btnReset = new Button("UPDATE PASSWORD");
        btnReset.getStyleClass().add("reset-submit-button");
        btnReset.setMaxWidth(Double.MAX_VALUE);

        btnReset.setOnAction(e -> {
            String email = txtEmail.getText().trim();
            String pass = txtNewPass.getText().trim();
            String confirm = txtConfirm.getText().trim();
            UserDAO dao = new UserDAO();

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                lblError.setText("Error: All fields are required.");
                return;
            }
            if (!dao.checkEmailExists(email)) {
                lblError.setText("Error: This email is not registered.");
                return;
            }
            if (!pass.equals(confirm)) {
                lblError.setText("Error: Passwords do not match.");
                return;
            }

            if (!STRONG_PASSWORD_PATTERN.matcher(pass).matches()) {
                lblError.setText("Password must be at least 8 chars, include letter, number, special char");
                return;
            }

            dao.updatePassword(email, pass);
            showSuccess("Success! Your password has been changed.");
            stage.close();
        });

        root.getChildren().addAll(
                titleLabel,
                new Label("Registered Email"), txtEmail,
                new Label("New Password"), txtNewPass,
                new Label("Confirm Password"), txtConfirm,
                lblError,
                btnReset);

        Scene scene = new Scene(root);
        try {
            String cssPath = getClass().getResource("/css/reset-dialog.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS file not found! Check your resources folder.");
        }

        stage.setScene(scene);
        stage.show();
    }

    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Success");

        VBox root = new VBox();
        root.getStyleClass().add("success-container");
        root.setPrefWidth(280);

        Label icon = new Label("✓");
        icon.getStyleClass().add("success-icon");

        Label message = new Label(msg);
        message.getStyleClass().add("success-text");
        message.setWrapText(true);

        Button btnClose = new Button("OK");
        btnClose.getStyleClass().add("success-button");
        btnClose.setPrefWidth(80);
        btnClose.setOnAction(e -> stage.close());

        root.getChildren().addAll(icon, message, btnClose);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/reset-dialog.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("Không load được CSS cho Success Dialog");
        }

        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showSuccessDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Registration successful! You can now log in.");
        alert.showAndWait();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showFieldError(Control field, Label errorLabel, String message) {
        if (!field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }

        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        Platform.runLater(() -> {
            field.requestFocus();

            if (field instanceof DatePicker datePicker) {
                datePicker.show();
            }

            if (field instanceof TextInputControl textInput) {
                textInput.positionCaret(textInput.getText().length());
            }
        });
    }

    private void clearFieldError(Control field, Label errorLabel) {
        field.getStyleClass().remove("input-error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearLoginErrors() {
        clearFieldError(txtUsername, lblUsernameError);
        clearFieldError(txtPassword, lblPasswordError);
    }

    private void clearRegisterErrors() {
        clearFieldError(txtFullName, lblFullNameError);
        clearFieldError(dpBirthDay, lblBirthDayError);
        clearFieldError(txtEmail, lblEmailError);
        clearFieldError(txtRegisterPassword, lblRegisterPasswordError);
    }

    private void showLoginPage() {
        clearLoginErrors();
        clearRegisterErrors();

        loginPage.setVisible(true);
        loginPage.setManaged(true);

        registerPage.setVisible(false);
        registerPage.setManaged(false);
    }

    private void showRegisterPage() {
        clearLoginErrors();
        clearRegisterErrors();

        registerPage.setVisible(true);
        registerPage.setManaged(true);

        loginPage.setVisible(false);
        loginPage.setManaged(false);
    }

    public TextField getTxtUsername() {
        return txtUsername;
    }

    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    public TextField getTxtFullName() {
        return txtFullName;
    }

    public DatePicker getDpBirthDay() {
        return dpBirthDay;
    }

    public TextField getTxtEmail() {
        return txtEmail;
    }

    public PasswordField getTxtRegisterPassword() {
        return txtRegisterPassword;
    }

    public RadioButton getRbMale() {
        return rbMale;
    }

    public RadioButton getRbFemale() {
        return rbFemale;
    }

    public Button getBtnOpenRegister() {
        return btnOpenRegister;
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Button getBtnBack() {
        return btnBack;
    }

    public Button getBtnCreate() {
        return btnCreate;
    }
}