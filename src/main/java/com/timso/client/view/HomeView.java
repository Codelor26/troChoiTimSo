package com.timso.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class HomeView extends StackPane {

    private final String playerName;
    private final String avatarPath;
    private final String username;
    private final java.sql.Timestamp lastNameChange;
    private InstructionView instructionView;

    private StackPane overlayPane;
    private VBox playerInfoPanel;

    public HomeView(String playerName, String avatarPath) {
    this(playerName, avatarPath, null, null);
}

public HomeView(String playerName, String avatarPath, String username, java.sql.Timestamp lastNameChange) {
    this.playerName = playerName;
    this.avatarPath = avatarPath;
    this.username = username;
    this.lastNameChange = lastNameChange;

    getStyleClass().add("auth-root");
    getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
    );

    getChildren().add(buildContent());
}

    private Node buildContent() {
        StackPane root = new StackPane();

        BorderPane mainContent = new BorderPane();
        mainContent.getStyleClass().add("home-root");
        mainContent.setTop(buildHeader());
        mainContent.setCenter(buildCenter());
        mainContent.setBottom(buildFooter());

        overlayPane = new StackPane();
        overlayPane.getStyleClass().add("overlay-pane");
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);

        playerInfoPanel = buildPlayerInfoPanel();
        overlayPane.getChildren().add(playerInfoPanel);

        overlayPane.setOnMouseClicked(e -> hidePlayerInfoPanel());
        playerInfoPanel.setOnMouseClicked(e -> e.consume());
        instructionView = new InstructionView(
            this::hideInstructionView,
            this::openGameView
    );

        root.getChildren().addAll(mainContent, overlayPane, instructionView);
        return root;
    }

    private Node buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.getStyleClass().add("home-header");

        HBox playerBox = new HBox(10);
        playerBox.setAlignment(Pos.CENTER_LEFT);

        ImageView avatarView = new ImageView(loadImage(avatarPath));
        avatarView.setFitWidth(52);
        avatarView.setFitHeight(52);
        avatarView.setPreserveRatio(true);

        Button btnAvatar = new Button();
        btnAvatar.setGraphic(avatarView);
        btnAvatar.getStyleClass().add("avatar-header-button");
        btnAvatar.setOnAction(e -> togglePlayerInfoPanel());

        Label lblName = new Label(playerName);
        lblName.getStyleClass().add("home-player-name");

        playerBox.getChildren().addAll(btnAvatar, lblName);

        Label lblTitle = new Label();
        lblTitle.textProperty().bind(I18n.bind("home_title"));
        lblTitle.getStyleClass().add("home-title");

        HBox coinBox = new HBox(10);
        coinBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblCoin = new Label("2000");
        lblCoin.getStyleClass().add("home-coin-text");

        ImageView coinIcon = new ImageView(loadImage("/icon/Hopstarter-Soft-Scraps-Coin.256.png"));
        coinIcon.setFitWidth(44);
        coinIcon.setFitHeight(44);
        coinIcon.setPreserveRatio(true);

        coinBox.getChildren().addAll(lblCoin, coinIcon);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        header.getChildren().addAll(playerBox, leftSpacer, lblTitle, rightSpacer, coinBox);
        return header;
    }

    private Node buildCenter() {
    StackPane center = new StackPane();
    center.setAlignment(Pos.CENTER);
    center.getStyleClass().add("home-center");

    Button btnMatch = new Button();
    btnMatch.textProperty().bind(I18n.bind("match"));
    btnMatch.getStyleClass().add("match-button");

    btnMatch.setOnAction(e -> {
        if (getScene() != null) {
            getScene().setRoot(new GameView(playerName, avatarPath));
        }
    });

        center.getChildren().add(btnMatch);
        return center;
    }   

    private Node buildFooter() {
        HBox footer = new HBox(90);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 0, 14, 0));
        footer.getStyleClass().add("home-footer");

        Button btnSetting = createFooterIconButton("/icon/Dtafalonso-Android-Lollipop-Settings.512.png");
        Button btnInstruction = createFooterIconButton("/icon/Vexels-Office-Rules.512.png");
        Button btnSkill = createFooterIconButton("/icon/Graphicloads-Folded-Thunder-folded.256.png");
        btnInstruction.setOnAction(e -> showInstructionView());
        btnSkill.setOnAction(e -> {
        if (getScene() != null) {
            getScene().setRoot(new Skill(playerName, avatarPath));
        }
        });
        btnSetting.setOnAction(e -> {
            System.out.println("Clicked setting");
            if (getScene() != null) {
                getScene().setRoot(new Setting(playerName, avatarPath));
            }
        });

        footer.getChildren().addAll(btnSetting, btnInstruction, btnSkill);
        return footer;
    }

    private VBox buildPlayerInfoPanel() {
       VBox panel = new VBox(18);
        panel.setPrefWidth(760);
        panel.setMaxWidth(760);

        panel.setPrefHeight(220);
        panel.setMaxHeight(220);

        panel.getStyleClass().add("player-info-panel");
        Label title = new Label();
        title.textProperty().bind(I18n.bind("player_info"));

        title.getStyleClass().add("player-info-title");

        Button btnClose = new Button("x");
        // btnClose.textProperty().bind(I18n.bind("close"));
        btnClose.getStyleClass().add("close-panel-button");
        btnClose.setOnAction(e -> hidePlayerInfoPanel());

        StackPane topBar = new StackPane();
        topBar.getChildren().addAll(title, btnClose);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);

        HBox content = new HBox(28);
        content.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView(loadImage(avatarPath));
        avatar.setFitWidth(80);
        avatar.setFitHeight(80);
        avatar.setPreserveRatio(true);

        Label lblName = new Label(playerName);
        lblName.getStyleClass().add("player-info-name");

        Label lblGold = new Label();
        lblGold.textProperty().bind(I18n.bind("gold_value"));
        lblGold.getStyleClass().add("player-info-text");

        Button btnEdit = new Button();
        btnEdit.textProperty().bind(I18n.bind("edit"));
        btnEdit.setOnAction(e -> {
        hidePlayerInfoPanel();

        com.timso.common.model.User user = new com.timso.common.model.User();
        user.setPlayerName(playerName);
        user.setAvatar(avatarPath);
        user.setUsername(username);
        user.setLastNameChange(lastNameChange);

        if (getScene() != null) {
            getScene().setRoot(new ProfileDialog(user));
        }
    });
        btnEdit.getStyleClass().add("profile-action-button");
        

        Button btnLogout = new Button();
        btnLogout.textProperty().bind(I18n.bind("logout"));
        btnLogout.getStyleClass().addAll("profile-action-button", "logout-button");
        btnLogout.setOnAction(e -> {
            if (getScene() != null) {
                getScene().setRoot(new LoginView());
            }
        });

        HBox actionRow = new HBox(10, btnEdit, btnLogout);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox leftInfo = new VBox(8, lblName, lblGold, actionRow);
        leftInfo.setAlignment(Pos.CENTER_LEFT);

        HBox profileBox = new HBox(14, avatar, leftInfo);
        profileBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox colGame = createStatColumn("game", LanguageManager.getString("find_number"));
        VBox colMatch = createStatColumn("match_count", "20");
        VBox colWin = createStatColumn("win", "10");
        VBox colRate = createStatColumn("rate", "50%");
        HBox statsRow = new HBox(28, colGame, colMatch, colWin, colRate);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getStyleClass().add("player-stats-row");

        content.getChildren().addAll(profileBox, spacer, statsRow);

        panel.getChildren().addAll(topBar, content);
        return panel;
    }

    private VBox createStatColumn(String titleKey, String value) {
        Label lblTitle = new Label();
        lblTitle.textProperty().bind(I18n.bind(titleKey));
        lblTitle.getStyleClass().add("player-stat-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("player-stat-value");

        VBox box = new VBox(10, lblTitle, lblValue);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("player-stat-box");
        return box;
    }

    private void togglePlayerInfoPanel() {
        boolean showing = overlayPane.isVisible();
        overlayPane.setVisible(!showing);
        overlayPane.setManaged(!showing);
    }

    private void hidePlayerInfoPanel() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
    }

    private Button createFooterIconButton(String iconPath) {
        ImageView iconView = new ImageView(loadImage(iconPath));
        iconView.setFitWidth(62);
        iconView.setFitHeight(62);
        iconView.setPreserveRatio(true);

        Button button = new Button();
        button.setGraphic(iconView);
        button.getStyleClass().add("footer-icon-button");
        return button;
    }
    private void showInstructionView() {
        hidePlayerInfoPanel();
        instructionView.showOverlay();
    }

    private void hideInstructionView() {
        instructionView.hideOverlay();
    }

    private void openGameView() {
        if (getScene() != null) {
            getScene().setRoot(new GameView(playerName, avatarPath));
        }
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm()
        );
    }
}