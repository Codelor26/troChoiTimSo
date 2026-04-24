package com.timso.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

import com.timso.common.model.User;
import com.timso.server.dao.UserDAO;

public class ProfileDialog extends StackPane {

    private final ImageView previewImage = new ImageView();
    private final TextField txtPlayerName = new TextField();
    private InstructionView instructionView;
    private final Label lblError = new Label("");
    private String selectedAvatarPath;
    private User currentUser;

    public ProfileDialog(User user) {
        this.currentUser = user;
        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        StackPane content = buildContent();
        instructionView = new InstructionView(this::hideInstructionView, this::openHomeView);

        getChildren().addAll(content, instructionView);
        LanguageManager.localeProperty().addListener((obs, oldVal, newVal) -> {
        if (getScene() != null) {
            getScene().setRoot(new ProfileDialog(currentUser));
        }
    });
    }

    private StackPane buildContent() {
        StackPane root = new StackPane();
        root.getStyleClass().add("screen-panel");
        Button btnClose = new Button("X");
        btnClose.getStyleClass().add("close-panel-button");

        btnClose.setOnAction(e -> {
            if (getScene() != null) {
                getScene().setRoot(new HomeView(
                    currentUser.getPlayerName(),
                    currentUser.getAvatar(),
                    currentUser.getUserName(),
                    currentUser.getLastNameChange()
                ));
            }
        });

        VBox card = new VBox(24);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("profile-card");

        Label title = new Label();
        title.textProperty().bind(I18n.bind("profile_title"));
        title.getStyleClass().add("profile-title");

        HBox topRow = new HBox(18);
        topRow.setAlignment(Pos.CENTER);

        previewImage.setFitWidth(72);
        previewImage.setFitHeight(72);
        previewImage.setPreserveRatio(true);
        previewImage.getStyleClass().add("profile-preview");

        txtPlayerName.promptTextProperty().bind(I18n.bind("enter_name"));
        txtPlayerName.getStyleClass().addAll("input-field", "profile-name-field");

        topRow.getChildren().addAll(previewImage, txtPlayerName);

        FlowPane avatarPane = new FlowPane();
        avatarPane.setHgap(18);
        avatarPane.setVgap(18);
        avatarPane.setAlignment(Pos.CENTER);
        avatarPane.setPadding(new Insets(14));
        avatarPane.getStyleClass().add("avatar-board");

        ToggleGroup avatarGroup = new ToggleGroup();

        String[] avatars = {
                "/icon/Martin-Berube-Character-Devil.256.png",
                "/icon/Iconarchive-Incognito-Animals-Raccoon-Avatar.512.png",
                "/icon/Hopstarter-Superhero-Avatar-Avengers-Thor.256.png",
                "/icon/Hopstarter-Superhero-Avatar-Avengers-Nick-Fury.256.png",
                "/icon/Hopstarter-Superhero-Avatar-Avengers-Giant-Man.256.png",
                "/icon/Hopstarter-Superhero-Avatar-Avengers-Black-Widow.256.png",
                "/icon/Diversity-Avatars-Avatars-Trinity.512.png",
                "/icon/Diversity-Avatars-Avatars-Nikola-tesla.512.png",
                "/icon/Diversity-Avatars-Avatars-Native-woman.512.png",
                "/icon/Diversity-Avatars-Avatars-Muslim-man.512.png",
                "/icon/Diversity-Avatars-Avatars-Mick-jagger.512.png",
                "/icon/Diversity-Avatars-Avatars-Luis-suarez.512.png",
                "/icon/Diversity-Avatars-Avatars-Joseph-stalin.512.png",
                "/icon/Diversity-Avatars-Avatars-Indian-woman.512.png",
                "/icon/Diversity-Avatars-Avatars-Dave-grohl.512.png",
                "/icon/Diversity-Avatars-Avatars-Charlie-chaplin.512.png"
        };

        for (String avatarPath : avatars) {
            avatarPane.getChildren().add(createAvatarButton(avatarPath, avatarGroup));
        }

        lblError.getStyleClass().add("profile-error-label");
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnStart = new Button();
        btnStart.textProperty().bind(I18n.bind("start"));
        btnStart.getStyleClass().addAll("action-button", "profile-start-button");
        btnStart.setOnAction(e -> handleStart());

        card.getChildren().addAll(title, topRow, avatarPane, lblError, btnStart);
        root.getChildren().addAll(card, btnClose);
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);
        StackPane.setMargin(btnClose, new Insets(20));

        return root;
    }

    private ToggleButton createAvatarButton(String avatarPath, ToggleGroup group) {
        ImageView avatarView = new ImageView(loadImage(avatarPath));
        avatarView.setFitWidth(64);
        avatarView.setFitHeight(64);
        avatarView.setPreserveRatio(true);

        ToggleButton button = new ToggleButton();
        button.setGraphic(avatarView);
        button.setToggleGroup(group);
        button.getStyleClass().add("avatar-button");

        button.setOnAction(e -> {
            selectedAvatarPath = avatarPath;
            previewImage.setImage(loadImage(avatarPath));
            clearError();
        });

        return button;
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
    }

    private void handleStart() {
        String playerName = txtPlayerName.getText() == null ? "" : txtPlayerName.getText().trim();
        

        if (selectedAvatarPath == null || selectedAvatarPath.isBlank()) {
            showError(LanguageManager.getString("choose_avatar"));
            return;
        }

        if (playerName.isEmpty()) {
            showError(LanguageManager.getString("name_required"));
            txtPlayerName.requestFocus();
            return;
        }

        if (!playerName.matches("^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$")) {
            showError(LanguageManager.getString("name_invalid"));
            txtPlayerName.requestFocus();
            return;
        }

        clearError();
        showInstructionView();
        System.out.println("[" + playerName + "]");
    }

    private void showInstructionView() {
        String newName = txtPlayerName.getText().trim();
        String newAvatar = selectedAvatarPath;
        boolean isNameChanged = !newName.equals(currentUser.getPlayerName());

        // 1. Kiểm tra ràng buộc 30 ngày nếu có đổi tên
        if (isNameChanged && currentUser.getPlayerName() != null && !currentUser.getPlayerName().isEmpty()) {
            java.sql.Timestamp lastChange = currentUser.getLastNameChange();
            
            if (lastChange != null) {
                long diffInMs = System.currentTimeMillis() - lastChange.getTime();
                long diffInDays = diffInMs / (1000 * 60 * 60 * 24);

                if (diffInDays < 30) {
                   showError(LanguageManager.getString("name_change_wait")
                        .replace("{days}", String.valueOf(30 - diffInDays)));
                    return; // Dừng lại không cho update
                }
            }
        }

        // 2. Gọi DAO để update (Cập nhật tham số isNameChanged để DAO biết có cần reset mốc 30 ngày không)
        UserDAO dao = new UserDAO();
        boolean success = dao.updateProfile(newName, newAvatar, currentUser.getUserName(), isNameChanged);

        if (success) {
            // Cập nhật lại object currentUser cục bộ để HomeView hiển thị đúng
            currentUser.setPlayerName(newName);
            currentUser.setAvatar(newAvatar);
            
            // Hiển thị overlay hướng dẫn
            instructionView.showOverlay();
        } else {
            showError(LanguageManager.getString("profile_update_error"));
        }
    }

    private void hideInstructionView() {
        instructionView.hideOverlay();
    }

    private void openHomeView() {
        if (getScene() != null) {
            getScene().setRoot(new HomeView(
                    txtPlayerName.getText().trim(),
                    selectedAvatarPath));
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void clearError() {
        lblError.setText("");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}