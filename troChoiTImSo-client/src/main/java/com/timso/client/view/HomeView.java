package com.timso.client.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.timso.client.network.GameClient;
import com.timso.common.model.User;

public class HomeView extends StackPane {

    private final String playerName;
    private final String avatarPath;
    private InstructionView instructionView;

    private StackPane overlayPane;
    private VBox playerInfoPanel;
    private GameClient gameClient;

    private Label waitingLabel;
    private StackPane findingMatchOverlay;
    private Timeline timeline;
    private int seconds = 0;

    public HomeView(String playerName, String avatarPath) {
        this.playerName = playerName;
        this.avatarPath = avatarPath;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        getChildren().add(buildContent());

        gameClient = GameClient.getInstance();
        gameClient.setListener(new GameClient.GameListener() {
            @Override
            public void onWaiting() {
                Platform.runLater(() -> {
                    if (waitingLabel != null) {
                        waitingLabel.setText("Đang tìm đối thủ...");
                    }
                });
            }

            @Override
            public void onTimeUpdate(int remainingSeconds) {
            }

            @Override
            public void onRematchRequest(String requester) {
                Platform.runLater(() -> {
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Đề nghị chơi lại");
                    confirmDialog.setHeaderText(requester + " muốn chơi lại!");
                    confirmDialog.setContentText("Bạn có đồng ý chơi lại không?");

                    ButtonType yesButton = new ButtonType("Đồng ý", ButtonBar.ButtonData.YES);
                    ButtonType noButton = new ButtonType("Từ chối", ButtonBar.ButtonData.NO);
                    confirmDialog.getButtonTypes().setAll(yesButton, noButton);

                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isPresent() && result.get() == yesButton) {
                        gameClient.requestRematch(); // Gửi đồng ý
                    } else {
                        gameClient.sendToServer("REJECT_REMATCH"); // Gửi từ chối
                    }
                });
            }

            @Override
            public void onRematchAccepted() {
            }

            @Override
            public void onRematchTimeout() {
            }

            @Override
            public void onRematchError(String error) {
            }

            @Override
            public void onWrong(int remainingSeconds) {
            }

            @Override
            public void onRematchWaiting(String message) {
            }

            @Override
            public void onBoardNumbers(String numbersStr) {
                System.out.println("HomeView received BOARD_NUMBERS, ignoring");
            }

            @Override
            public void onMatchFound(String opponentName, String opponentAvatar) {
                System.out.println("=== onMatchFound CALLED! opponent: " + opponentName + " ===");

                gameClient.setListener(null);

                Platform.runLater(() -> {
                    System.out.println("Platform.runLater - creating GameView");
                    stopFindingTimer();
                    if (findingMatchOverlay != null) {
                        findingMatchOverlay.setVisible(false);
                    }
                    if (getScene() != null) {
                        System.out.println("getScene() not null, creating GameView");
                        GameView gameView = new GameView(playerName, avatarPath, opponentName, opponentAvatar);
                        getScene().setRoot(gameView);
                        System.out.println("GameView set as root successfully");
                    } else {
                        System.err.println("getScene() is NULL! Cannot switch to GameView");
                    }
                });
            }

            @Override
            public void onGameStart(int currentTarget, int duration) {
            }

            @Override
            public void onCorrect(String playerName, int number, int score) {
            }

            @Override
            public void onNewTarget(int target) {
            }

            @Override
            public void onGameOver(String winner, int yourScore, int opponentScore) {
            }

            @Override
            public void onGoldUpdate(int goldAdded, int newGold) {
                Platform.runLater(() -> {
                    PlayerSession.updateGold(newGold);
                });

            }

            @Override
            public void onGameEnd(String message) {
                Platform.runLater(() -> {
                    Toast.show(HomeView.this, message, 2000);
                    if (getScene() != null) {
                        getScene().setRoot(new HomeView(playerName, avatarPath));
                    }
                });
            }

            @Override
            public void onRematchRejected(String rejecter) {
                Platform.runLater(() -> {
                    Toast.show(HomeView.this, "❌ " + rejecter + " đã từ chối chơi lại", 2000);

                    if (getScene() != null) {
                        getScene().setRoot(new HomeView(playerName, avatarPath));
                    }
                });
            }

            @Override
            public void onRematchCancelled() {
                Platform.runLater(() -> {
                    Toast.show(HomeView.this, "Đã hủy yêu cầu chơi lại", 2000);

                    if (getScene() != null) {
                        getScene().setRoot(new HomeView(playerName, avatarPath));
                    }
                });
            }

            @Override
            public void onLuckyEvent(int luckyNumber, int duration) {
            }

            @Override
            public void onLuckyEventEnd() {
            }

            @Override
            public void onLuckyBonus(String playerName, int number, String bonus) {
            }

            @Override
            public void onFreezePlayer(int duration) {
            }

            @Override
            public void onUnfreezePlayer() {
            }

            @Override
            public void onBlockNumbers(int duration) {
            }

            @Override
            public void onUnblockNumbers() {
            }

            @Override
            public void onVideoRewardSuccess(int rewardAmount, int newGold) {
            }

            @Override
            public void onVideoRewardFail(String message) {
            }
        });

    }

    private void showFindingMatchUI() {
        if (findingMatchOverlay == null) {
            findingMatchOverlay = new StackPane();
            findingMatchOverlay.getStyleClass().add("overlay-pane");

            VBox box = new VBox(15);
            box.setAlignment(Pos.CENTER);

            waitingLabel = new Label("Đang tìm trận: 0s");
            waitingLabel.getStyleClass().add("finding-text");

            Button btnCancel = new Button("Hủy");
            btnCancel.setOnAction(e -> cancelFindingMatch());

            box.getChildren().addAll(waitingLabel, btnCancel);
            findingMatchOverlay.getChildren().add(box);

            getChildren().add(findingMatchOverlay);
        }

        findingMatchOverlay.setVisible(true);
        startFindingTimer();
    }

    private void startFindingTimer() {
        seconds = 0;

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    seconds++;
                    if (waitingLabel != null) {
                        waitingLabel.setText("Đang tìm trận: " + seconds + "s");
                    }
                }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void stopFindingTimer() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public HomeView(String playerName, String avatarPath, User user) {
        this(playerName, avatarPath);
        if (user != null) {
            PlayerSession.setCurrentUser(user);
        }
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
                this::openGameView);

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

        LanguageManager lang = LanguageManager.getInstance();
        Label lblTitle = new Label(lang.getString("home.title"));
        lblTitle.getStyleClass().add("home-title");

        HBox coinBox = new HBox(10);
        coinBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblCoin = new Label(String.valueOf(PlayerSession.getGold()));
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

        LanguageManager lang = LanguageManager.getInstance();
        Button btnMatch = new Button(lang.getString("home.match"));
        btnMatch.getStyleClass().add("match-button");

        btnMatch.setOnAction(e -> {
            if (gameClient.isConnected()) {
                gameClient.findMatch();
                showFindingMatchUI();
            } else {
                System.err.println("GameClient not connected! Please restart.");
                showConnectionError();
            }
        });

        center.getChildren().add(btnMatch);
        return center;
    }

    private void showConnectionError() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Lỗi kết nối");
        alert.setHeaderText(null);
        alert.setContentText("Không thể kết nối đến server. Vui lòng đăng nhập lại.");
        alert.showAndWait();
    }

    private void cancelFindingMatch() {
        gameClient.cancelMatch();
        stopFindingTimer();
        if (findingMatchOverlay != null) {
            findingMatchOverlay.setVisible(false);
        }
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
            if (getScene() != null) {
                User u = PlayerSession.getCurrentUser();
                if (u != null) {
                    getScene().setRoot(new Setting(playerName, avatarPath, u.getUserName()));
                } else {
                    getScene().setRoot(new Setting(playerName, avatarPath));
                }
            }
        });

        footer.getChildren().addAll(btnSetting, btnInstruction, btnSkill);
        return footer;
    }

    private VBox buildPlayerInfoPanel() {
        LanguageManager lang = LanguageManager.getInstance();

        VBox panel = new VBox(18);
        panel.setPrefWidth(760);
        panel.setMaxWidth(760);

        panel.setPrefHeight(220);
        panel.setMaxHeight(220);

        panel.getStyleClass().add("player-info-panel");
        Label title = new Label(lang.getString("home.player.info"));
        title.getStyleClass().add("player-info-title");

        Button btnClose = new Button("X");
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

        Label lblGold = new Label(lang.getString("home.gold") + PlayerSession.getGold());
        lblGold.getStyleClass().add("player-info-text");

        Button btnEdit = new Button(lang.getString("home.button.edit"));
        btnEdit.getStyleClass().add("profile-action-button");
        btnEdit.setOnAction(e -> {
            User u = PlayerSession.getCurrentUser();
            if (u != null) {
                EditProfileDialog dialog = new EditProfileDialog(u);
                dialog.setOnSuccess(this::updateUIFromSession);
                dialog.show();
            }
        });

        Button btnLogout = new Button(lang.getString("home.button.logout"));
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

        VBox colGame = createStatColumn(lang.getString("home.stat.game"), lang.getString("home.stat.find.number"));
        VBox colMatch = createStatColumn(lang.getString("home.stat.match"), "20");
        VBox colWin = createStatColumn(lang.getString("home.stat.win"), "10");
        VBox colRate = createStatColumn(lang.getString("home.stat.rate"), "50%");

        HBox statsRow = new HBox(28, colGame, colMatch, colWin, colRate);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getStyleClass().add("player-stats-row");

        content.getChildren().addAll(profileBox, spacer, statsRow);

        panel.getChildren().addAll(topBar, content);
        return panel;
    }

    private void updateUIFromSession() {
        if (getScene() != null) {
            User u = PlayerSession.getCurrentUser();
            getScene().setRoot(new HomeView(u.getPlayerName(), u.getAvatar(), u));
        }
    }

    private VBox createStatColumn(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("player-stat-title");
        lblTitle.setWrapText(true);
        lblTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblTitle.setMaxWidth(100);

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("player-stat-value");
        lblValue.setWrapText(true);
        lblValue.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblValue.setMaxWidth(100);

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
            // getScene().setRoot(new GameView(playerName, avatarPath));
        }
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
    }
}