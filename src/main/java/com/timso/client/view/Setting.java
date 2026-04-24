package com.timso.client.view;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

        Label title = new Label();
        title.textProperty().bind(I18n.bind("setting"));        title.getStyleClass().add("setting-title");

        VBox soundBox = buildSoundSection();
        VBox profileBox = buildProfileSection();
        VBox languageBox = buildLanguageSection();
        VBox feedbackBox = buildFeedbackSection();

        lblError.getStyleClass().add("setting-error-label");
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnSave = new Button();
        btnSave.textProperty().bind(I18n.bind("save"));
        btnSave.getStyleClass().addAll("action-button", "setting-main-button");
        btnSave.setOnAction(e -> saveAndBackHome());

        Button btnBack = new Button();
        btnBack.textProperty().bind(I18n.bind("back"));
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
        VBox box = createSectionBoxByKey("sound");

        btnMusic.getStyleClass().add("setting-toggle-button");
        btnEffect.getStyleClass().add("setting-toggle-button");

        btnMusic.setSelected(true);
        btnEffect.setSelected(true);

        btnMusic.textProperty().bind(
        javafx.beans.binding.Bindings.when(btnMusic.selectedProperty())
                .then(I18n.bind("toggle_on"))
                .otherwise(I18n.bind("toggle_off"))
        );

        btnEffect.textProperty().bind(
                javafx.beans.binding.Bindings.when(btnEffect.selectedProperty())
                        .then(I18n.bind("toggle_on"))
                        .otherwise(I18n.bind("toggle_off"))
        );
        
        Label lblMusic = new Label();
        lblMusic.textProperty().bind(I18n.bind("music"));
        lblMusic.getStyleClass().add("setting-section-label");

        Label lblEffect = new Label();
        lblEffect.textProperty().bind(I18n.bind("effect"));
        lblEffect.getStyleClass().add("setting-section-label");
        HBox musicRow = new HBox(18, lblMusic, btnMusic);
        musicRow.setAlignment(Pos.CENTER_LEFT);

        HBox effectRow = new HBox(18, lblEffect, btnEffect);
        effectRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(musicRow, effectRow);
        return box;
    }

    private VBox buildProfileSection() {
        VBox box = createSectionBoxByKey("profile");

        txtPlayerName.setText(currentPlayerName);
        txtPlayerName.promptTextProperty().bind(I18n.bind("enter_name"));
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
    VBox box = createSectionBoxByKey("language");

    cboLanguage.getItems().clear();
    cboLanguage.getItems().addAll("vi", "en");
    cboLanguage.setValue(LanguageManager.getLocale().getLanguage());
    cboLanguage.getStyleClass().add("setting-combo-box");

    cboLanguage.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.equals("vi")
                    ? LanguageManager.getString("lang_vi")
                    : LanguageManager.getString("lang_en"));
            }
        }
    });

    cboLanguage.setButtonCell(new javafx.scene.control.ListCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.equals("vi")
                        ? LanguageManager.getString("lang_vi")
                        : LanguageManager.getString("lang_en"));
            }
        }
    });

    cboLanguage.setOnAction(e -> {
        String selectedLang = cboLanguage.getValue();
        LanguageManager.setLocale(selectedLang);
    });

    Label lblLang = new Label();
    lblLang.textProperty().bind(I18n.bind("choose_language"));
    lblLang.getStyleClass().add("setting-section-label");
    HBox row = new HBox(
            18,
            lblLang,
            cboLanguage
    );
    row.setAlignment(Pos.CENTER_LEFT);

    box.getChildren().add(row);
    return box;
}

    private VBox buildFeedbackSection() {
        VBox box = createSectionBoxByKey("feedback");

        Label desc = new Label();
        desc.textProperty().bind(I18n.bind("feedback_desc"));
        desc.getStyleClass().add("setting-section-label");
        desc.setWrapText(true);

        Button btnMail = new Button();
        btnMail.textProperty().bind(I18n.bind("contact_mail"));        btnMail.getStyleClass().addAll("action-button", "setting-mail-button");
        btnMail.setOnAction(e -> openFeedbackMail());

        box.getChildren().addAll(desc, btnMail);
        return box;
    }

    private VBox createSectionBoxByKey(String key) {
    Label title = new Label();
    title.textProperty().bind(I18n.bind(key));
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

    private void saveAndBackHome() {
        String newName = txtPlayerName.getText() == null ? "" : txtPlayerName.getText().trim();

        if (newName.isEmpty()) {
            showError(LanguageManager.getString("name_required"));
            txtPlayerName.requestFocus();
            return;
        }

        if (!newName.matches("^[\\p{L}\\s]+$")) {
            showError(LanguageManager.getString("name_invalid"));
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

    // private void reloadUI() {
    //     if (getScene() != null) {
    //         getScene().setRoot(new Setting(currentPlayerName, currentAvatarPath));
    //     }
    // }

    private void openFeedbackMail() {
        try {
            if (Desktop.isDesktopSupported()) {
                URI mailUri = new URI(
                        "mailto:timso.support@gmail.com?subject=Feedback%20Find%20Number%20Game"
                );
                Desktop.getDesktop().mail(mailUri);
            } else {
                showError(LanguageManager.getString("mail_not_supported"));
            }
        } catch (Exception ex) {
            showError(LanguageManager.getString("mail_open_failed"));
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