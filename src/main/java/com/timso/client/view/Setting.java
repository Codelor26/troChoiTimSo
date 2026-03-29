package com.timso.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

import java.awt.Desktop;
import java.net.URI;
import java.util.Objects;
import javafx.scene.control.ScrollPane;

public class Setting extends StackPane {

    private String currentPlayerName;
    private String currentAvatarPath;

    private final TextField txtPlayerName = new TextField();
    private final ComboBox<String> cboLanguage = new ComboBox<>();
    private final ToggleButton btnMusic = new ToggleButton();
    private final ToggleButton btnEffect = new ToggleButton();
    private final ImageView avatarPreview = new ImageView();
    private final Label lblError = new Label("");

    public Setting(String playerName, String avatarPath) {
        this.currentPlayerName = playerName;
        this.currentAvatarPath = avatarPath;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );

        getChildren().add(buildContent());
    }

    private Node buildContent() {
        StackPane root = new StackPane();
        root.getStyleClass().add("setting-root");

        VBox panel = new VBox(14);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(760);
        panel.setMaxWidth(760);
        panel.getStyleClass().add("setting-panel");
        panel.setPadding(new Insets(22, 26, 24, 26));

        Label title = new Label("Cài đặt");
        title.getStyleClass().add("setting-title");

        VBox soundBox = buildSoundSection();
        VBox profileBox = buildProfileSection();
        VBox languageBox = buildLanguageSection();
        VBox feedbackBox = buildFeedbackSection();

        lblError.getStyleClass().add("setting-error-label");
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnSave = new Button("Lưu");
        btnSave.getStyleClass().addAll("action-button", "setting-main-button");
        btnSave.setOnAction(e -> saveAndBackHome());

        Button btnBack = new Button("Quay lại");
        btnBack.getStyleClass().addAll("action-button", "setting-main-button");
        btnBack.setOnAction(e -> backHome());

        HBox actionRow = new HBox(16, btnBack, btnSave);
        actionRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(
                title,
                soundBox,
                profileBox,
                languageBox,
                feedbackBox,
                lblError,
                actionRow
        );
         StackPane contentWrapper = new StackPane(panel);
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(contentWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("setting-scroll-pane");

        root.getChildren().add(scrollPane);
        return root;
        
    }

    private VBox buildSoundSection() {
        VBox box = createSectionBox("Âm thanh");

        updateToggleText(btnMusic, true);
        updateToggleText(btnEffect, true);

        btnMusic.getStyleClass().add("setting-toggle-button");
        btnEffect.getStyleClass().add("setting-toggle-button");

        btnMusic.setSelected(true);
        btnEffect.setSelected(true);

        btnMusic.setOnAction(e -> updateToggleText(btnMusic, btnMusic.isSelected()));
        btnEffect.setOnAction(e -> updateToggleText(btnEffect, btnEffect.isSelected()));

        HBox musicRow = new HBox(18, createSectionLabel("Music"), btnMusic);
        musicRow.setAlignment(Pos.CENTER_LEFT);

        HBox effectRow = new HBox(18, createSectionLabel("Effect"), btnEffect);
        effectRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(musicRow, effectRow);
        return box;
    }

    private VBox buildProfileSection() {
        VBox box = createSectionBox("Đổi tên + avatar");

        txtPlayerName.setText(currentPlayerName);
        txtPlayerName.setPromptText("Nhập tên mới");
        txtPlayerName.getStyleClass().addAll("input-field", "setting-name-field");

        avatarPreview.setImage(loadImage(currentAvatarPath));
        avatarPreview.setFitWidth(56);
        avatarPreview.setFitHeight(56);
        avatarPreview.setPreserveRatio(true);
        avatarPreview.getStyleClass().add("setting-avatar-preview");

        HBox topRow = new HBox(18, avatarPreview, txtPlayerName);
        topRow.setAlignment(Pos.CENTER_LEFT);

        FlowPane avatarGrid = new FlowPane();
        avatarGrid.setHgap(12);
        avatarGrid.setVgap(12);
        avatarGrid.setAlignment(Pos.CENTER_LEFT);
        avatarGrid.getStyleClass().add("setting-avatar-grid");

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
            ToggleButton avatarButton = createAvatarButton(avatarPath, avatarGroup);
            avatarGrid.getChildren().add(avatarButton);

            if (avatarPath.equals(currentAvatarPath)) {
                avatarButton.setSelected(true);
            }
        }

        box.getChildren().addAll(topRow, avatarGrid);
        return box;
    }

    private VBox buildLanguageSection() {
        VBox box = createSectionBox("Ngôn ngữ");

        cboLanguage.getItems().addAll("Tiếng Việt", "English");
        cboLanguage.setValue("Tiếng Việt");
        cboLanguage.getStyleClass().add("setting-combo-box");

        HBox row = new HBox(18, createSectionLabel("Chọn ngôn ngữ"), cboLanguage);
        row.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().add(row);
        return box;
    }

    private VBox buildFeedbackSection() {
        VBox box = createSectionBox("Báo lỗi và góp ý");

        Label desc = new Label("Nhấn nút bên dưới để gửi email góp ý hoặc báo lỗi cho người phát triển.");
        desc.getStyleClass().add("setting-feedback-text");
        desc.setWrapText(true);

        Button btnMail = new Button("Liên hệ qua mail");
        btnMail.getStyleClass().addAll("action-button", "setting-mail-button");
        btnMail.setOnAction(e -> openFeedbackMail());

        box.getChildren().addAll(desc, btnMail);
        return box;
    }

    private VBox createSectionBox(String titleText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("setting-section-title");

        VBox box = new VBox(14, title);
        box.getStyleClass().add("setting-section-box");
        return box;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("setting-section-label");
        return label;
    }

    private ToggleButton createAvatarButton(String avatarPath, ToggleGroup group) {
        ImageView imageView = new ImageView(loadImage(avatarPath));
        imageView.setFitWidth(34);
        imageView.setFitHeight(34);
        imageView.setPreserveRatio(true);

        ToggleButton button = new ToggleButton();
        button.setGraphic(imageView);
        button.setToggleGroup(group);
        button.getStyleClass().add("setting-avatar-button");

        button.setOnAction(e -> {
            currentAvatarPath = avatarPath;
            avatarPreview.setImage(loadImage(avatarPath));
            hideError();
        });

        return button;
    }

    private void updateToggleText(ToggleButton button, boolean selected) {
        button.setText(selected ? "ON" : "OFF");
    }

    private void saveAndBackHome() {
        String newName = txtPlayerName.getText() == null ? "" : txtPlayerName.getText().trim();

        if (newName.isEmpty()) {
            showError("Vui lòng nhập tên.");
            txtPlayerName.requestFocus();
            return;
        }

        if (!newName.matches("^[\\p{L}\\s]+$")) {
            showError("Tên chỉ được chứa chữ cái và khoảng trắng.");
            txtPlayerName.requestFocus();
            return;
        }

        currentPlayerName = newName;
        hideError();
        backHome();
    }

    private void backHome() {
        if (getScene() != null) {
            getScene().setRoot(new HomeView(currentPlayerName, currentAvatarPath));
        }
    }

    private void openFeedbackMail() {
        try {
            if (Desktop.isDesktopSupported()) {
                URI mailUri = new URI(
                        "mailto:timso.support@gmail.com?subject=Feedback%20Find%20Number%20Game"
                );
                Desktop.getDesktop().mail(mailUri);
            } else {
                showError("Máy hiện tại không hỗ trợ mở ứng dụng mail.");
            }
        } catch (Exception ex) {
            showError("Không thể mở mail mặc định.");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setText("");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm()
        );
    }
}