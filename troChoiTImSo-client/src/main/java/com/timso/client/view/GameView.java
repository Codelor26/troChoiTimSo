package com.timso.client.view;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import com.timso.client.network.GameClient;

public class GameView extends StackPane implements GameClient.GameListener {

    private static final Random RANDOM = new Random();

    private String playerName;
    private String avatarPath;
    private int currentTarget;

    private final Map<Integer, Label> numberLabelMap = new HashMap<>();

    private Pane boardPane;
    private StackPane skillOverlay;
    private VBox skillPanel;

    private String opponentName;
    private String opponentAvatar;

    private GameClient gameClient;
    private Label lblTarget;
    private Label lblTimer;
    private Label lblLeftScore;
    private Label lblRightScore;
    private Timeline timerTimeline;
    private StackPane gameOverOverlay;
    private StackPane rematchOverlay;
    private Label waitingLabel;
    private StackPane blockOverlay;

    private int timeRemaining = 120;
    private int myScore = 0;
    private int opponentScore = 0;

    private boolean isFrozen = false;
    private boolean isWaitingForRematch = false;

    private List<Integer> boardNumbers;

    public GameView(String playerName, String avatarPath) {
        this(playerName, avatarPath, "Đối thủ", "");
    }

    public GameView(String playerName, String avatarPath, String opponentName, String opponentAvatar) {
        this.playerName = playerName;
        this.avatarPath = avatarPath;
        this.opponentName = opponentName;
        this.opponentAvatar = opponentAvatar;
        this.currentTarget = 0;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        getChildren().add(buildContent());

        initGameClient();
    }

    private void initGameClient() {
        gameClient = GameClient.getInstance();
        gameClient.setListener(this);
        System.out.println("GameView listener set, ready to receive BOARD_NUMBERS");
        System.out.println("Current skills in GameView - Light: " + PlayerSession.getLightSkill() +
                ", Dark: " + PlayerSession.getDarkSkill() +
                ", Freeze: " + PlayerSession.getFreezeSkill());

    }

    private Node buildContent() {
        StackPane root = new StackPane();

        BorderPane mainContent = new BorderPane();
        mainContent.getStyleClass().add("game-root");
        mainContent.setTop(buildHeader());
        mainContent.setCenter(buildBoardArea());

        skillOverlay = new StackPane();
        skillOverlay.getStyleClass().add("overlay-pane");
        skillOverlay.setVisible(false);
        skillOverlay.setManaged(false);

        skillPanel = buildSkillPanel();
        skillOverlay.getChildren().add(skillPanel);

        skillOverlay.setOnMouseClicked(e -> hideSkillPanel());
        skillPanel.setOnMouseClicked(e -> e.consume());

        root.getChildren().addAll(mainContent, skillOverlay);
        return root;
    }

    private Node buildHeader() {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(10, 18, 8, 18));
        header.getStyleClass().add("game-header");

        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        ImageView leftAvatar = new ImageView(loadImage(avatarPath));
        leftAvatar.setFitWidth(42);
        leftAvatar.setFitHeight(42);
        leftAvatar.setPreserveRatio(true);

        lblLeftScore = new Label("0");
        lblLeftScore.getStyleClass().add("game-player-label");

        Label lblLeftName = new Label(playerName);
        lblLeftName.getStyleClass().add("game-player-label");

        leftBox.getChildren().addAll(leftAvatar, lblLeftName, lblLeftScore);

        VBox centerBox = new VBox(2);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setFillWidth(false);

        lblTarget = new Label(String.valueOf(currentTarget));
        lblTarget.getStyleClass().add("target-number-label");

        StackPane targetCircle = new StackPane(lblTarget);
        targetCircle.getStyleClass().add("target-circle");
        targetCircle.setMinSize(56, 56);
        targetCircle.setPrefSize(56, 56);
        targetCircle.setMaxSize(56, 56);

        lblTimer = new Label("2:00");
        lblTimer.getStyleClass().add("game-timer-label");

        centerBox.getChildren().addAll(targetCircle, lblTimer);

        HBox rightBox = new HBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        lblRightScore = new Label("0");
        lblRightScore.getStyleClass().add("game-player-label");

        Label lblRightName = new Label(opponentName);
        lblRightName.getStyleClass().add("game-player-label");

        ImageView rightAvatar = new ImageView(
                loadImage(opponentAvatar != null && !opponentAvatar.isEmpty() ? opponentAvatar
                        : "/icon/Martin-Berube-Character-Devil.256.png"));
        rightAvatar.setFitWidth(42);
        rightAvatar.setFitHeight(42);
        rightAvatar.setPreserveRatio(true);

        ImageView myMarkIcon = new ImageView(
                loadImage("/icon/Gakuseisean-Ivista-2-Start-Menu-Search.256.png"));
        myMarkIcon.setFitWidth(26);
        myMarkIcon.setFitHeight(26);
        myMarkIcon.setPreserveRatio(true);
        myMarkIcon.getStyleClass().add("my-player-mark");

        Button btnMySkill = new Button();
        btnMySkill.setGraphic(myMarkIcon);
        btnMySkill.getStyleClass().add("avatar-header-button");
        btnMySkill.setOnAction(e -> toggleSkillPanel());

        rightBox.getChildren().addAll(lblRightScore, lblRightName, rightAvatar, btnMySkill);

        header.setLeft(leftBox);
        header.setCenter(centerBox);
        header.setRight(rightBox);

        return header;
    }

    private Node buildBoardArea() {
        StackPane boardArea = new StackPane();

        boardPane = new Pane();
        boardPane.getStyleClass().add("game-board");

        Button btnBack = new Button("←");
        btnBack.getStyleClass().add("game-back-button");
        btnBack.setOnAction(e -> {
            if (gameClient != null && gameClient.isConnected()) {
                gameClient.leaveGame();
            }

            if (timerTimeline != null) {
                timerTimeline.stop();
            }

            if (getScene() != null) {
                getScene().setRoot(new HomeView(playerName, avatarPath));
            }
        });
        btnBack.setLayoutX(16);
        btnBack.setLayoutY(16);
        boardPane.getChildren().add(btnBack);

        boardPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (boardPane.getWidth() > 0 && boardPane.getHeight() > 0 && boardNumbers != null) {
                drawBoard();
            }
        });
        boardPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (boardPane.getWidth() > 0 && boardPane.getHeight() > 0 && boardNumbers != null) {
                drawBoard();
            }
        });

        boardArea.getChildren().add(boardPane);
        return boardArea;
    }

    private void drawBoard() {
        if (boardNumbers == null || boardPane == null)
            return;

        double boardWidth = boardPane.getWidth();
        double boardHeight = boardPane.getHeight();

        if (boardWidth <= 0 || boardHeight <= 0)
            return;

        boardPane.getChildren().removeIf(node -> node instanceof Label);
        numberLabelMap.clear();

        List<Point2D> positions = generateRandomPositions(boardNumbers.size(), boardWidth, boardHeight);

        for (int i = 0; i < boardNumbers.size() && i < positions.size(); i++) {
            int number = boardNumbers.get(i);

            Label lbl = new Label(String.valueOf(number));
            lbl.getStyleClass().add("board-number");
            lbl.setCursor(Cursor.HAND);

            Point2D point = positions.get(i);
            lbl.setLayoutX(point.getX());
            lbl.setLayoutY(point.getY());

            int finalNumber = number;
            lbl.setOnMouseClicked(e -> handleNumberClick(finalNumber, lbl));

            numberLabelMap.put(number, lbl);
            boardPane.getChildren().add(lbl);
        }

        System.out.println("Drew board with " + boardNumbers.size() + " numbers");
    }

    private void handleNumberClick(int number, Label label) {
        if (label.getStyleClass().contains("board-number-correct") ||
                label.getStyleClass().contains("board-number-opponent")) {
            return;
        }

        if (label.getStyleClass().contains("board-number-wrong")) {
            return;
        }

        if (gameClient != null && gameClient.isConnected()) {
            System.out.println("Clicked number: " + number + ", currentTarget: " + currentTarget);
            gameClient.pickNumber(number);
        } else {
            System.out.println("GameClient not connected!");
        }
    }

    private void updateTarget(int target) {
        this.currentTarget = target;
        if (lblTarget != null) {
            lblTarget.setText(String.valueOf(target));
        }
    }

    private void updateScore(String playerName, int score) {
        if (playerName.equals(this.playerName)) {
            myScore = score;
            if (lblLeftScore != null) {
                lblLeftScore.setText(String.valueOf(score));
            }
        } else {
            opponentScore = score;
            if (lblRightScore != null) {
                lblRightScore.setText(String.valueOf(score));
            }
        }
    }

    private void startCountdown() {
        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        timerTimeline = new Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1), e -> {
                    if (timeRemaining > 0) {
                        timeRemaining--;
                        int minutes = timeRemaining / 60;
                        int seconds = timeRemaining % 60;
                        if (lblTimer != null) {
                            lblTimer.setText(String.format("%d:%02d", minutes, seconds));
                        }
                    }
                    if (timeRemaining <= 0) {
                        timerTimeline.stop();
                    }
                }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void showGameOverUI(String winner, int yourScore, int opponentScore) {
        LanguageManager lang = LanguageManager.getInstance();

        System.out.println("=== GAME_OVER RECEIVED ===");
        System.out.println("Winner: " + winner);
        System.out.println("Your score: " + yourScore);
        System.out.println("Opponent score: " + opponentScore);

        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        Platform.runLater(() -> {
            gameOverOverlay = new StackPane();
            gameOverOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
            gameOverOverlay.setAlignment(Pos.CENTER);

            VBox dialog = new VBox(20);
            dialog.getStyleClass().add("game-over-dialog");
            dialog.setAlignment(Pos.CENTER);

            String titleText;
            String iconEmoji;

            if ("DRAW".equals(winner)) {
                titleText = lang.getString("gameview.draw");
                iconEmoji = "🤝"; // thay hinh anh
            } else if (winner.equals(this.playerName)) {
                titleText = lang.getString("gameview.win");
                iconEmoji = "🏆";
                if (!isWaitingForRematch)
                    showConfetti();
            } else {
                titleText = lang.getString("gameview.lose");
                iconEmoji = "💔";
            }

            if (winner.equals(this.playerName)) {
                SoundManager.playSound("win.mp3");
            } else if ("DRAW".equals(winner)) {
                SoundManager.playSound("draw.mp3");
            } else {
                SoundManager.playSound("lose.mp3");
            }

            Label lblIcon = new Label(iconEmoji);
            lblIcon.setStyle("-fx-font-size: 60px;");

            Label lblTitle = new Label(titleText);
            lblTitle.getStyleClass().add("game-over-title");

            HBox scoreBox = new HBox(30);
            scoreBox.setAlignment(Pos.CENTER);

            VBox yourScoreBox = createScoreBox(lang.getString("gameview.you"), yourScore,
                    winner.equals(this.playerName));
            VBox opponentScoreBox = createScoreBox(lang.getString("gameview.opponent"), opponentScore,
                    !winner.equals(this.playerName) && !"DRAW".equals(winner));

            Label lblVS = new Label("VS");
            lblVS.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");

            scoreBox.getChildren().addAll(yourScoreBox, lblVS, opponentScoreBox);

            Button btnRequestRematch = new Button("🔄 " + lang.getString("gameview.offer") + " 🔄");
            btnRequestRematch.getStyleClass().add("game-over-button");
            btnRequestRematch.setOnAction(e -> {
                gameClient.requestRematch();
                showWaitingForRematchDialog();
                removeGameOverOverlay();
            });

            Button btnBackHome = new Button("🏠 " + lang.getString("gameview.backhome") + " 🏠");
            btnBackHome.getStyleClass().add("game-over-button");
            btnBackHome.setStyle("-fx-background-color: linear-gradient(to bottom, #4a5568, #2d3748);");
            btnBackHome.setOnAction(e -> {
                if (getScene() != null) {
                    getScene().setRoot(new HomeView(playerName, avatarPath));
                }
            });

            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(btnRequestRematch, btnBackHome);

            dialog.getChildren().addAll(lblIcon, lblTitle, scoreBox, buttonBox);
            if (winner.equals(this.playerName)) {
                Label lblGoldReward = new Label(lang.getString("gameview.plus.gold"));
                lblGoldReward.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
                dialog.getChildren().add(3, lblGoldReward);
            }
            gameOverOverlay.getChildren().add(dialog);

            gameOverOverlay.setOpacity(0);
            getChildren().add(gameOverOverlay);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500),
                    gameOverOverlay);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });

    }

    private void showWaitingForRematchDialog() {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            rematchOverlay = new StackPane();
            rematchOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
            rematchOverlay.setAlignment(Pos.CENTER);

            VBox dialog = new VBox(20);
            dialog.getStyleClass().add("game-over-dialog");
            dialog.setAlignment(Pos.CENTER);

            Label lblIcon = new Label("⏳");
            lblIcon.setStyle("-fx-font-size: 60px;");

            Label lblTitle = new Label(lang.getString("gameview.waitfor.opponent"));
            lblTitle.getStyleClass().add("game-over-title");
            lblTitle.setStyle("-fx-font-size: 28px;");

            Label lblWaiting = new Label(
                    lang.getString("gameview.waiting") + " " + opponentName + " "
                            + lang.getString("gameview.agree.play.again"));
            lblWaiting.setStyle("-fx-font-size: 18px; -fx-text-fill: #cccccc;");

            javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
            progress.setMaxSize(50, 50);

            Button btnCancel = new Button("❌ " + lang.getString("gameview.cancer.request") + " ❌");
            btnCancel.getStyleClass().add("game-over-button");
            btnCancel.setStyle("-fx-background-color: linear-gradient(to bottom, #ff6b6b, #ee5a24);");
            btnCancel.setOnAction(e -> {
                gameClient.cancelRematch();
                removeRematchOverlay();
                showGameOverUI("DRAW", myScore, opponentScore);
            });

            dialog.getChildren().addAll(lblIcon, lblTitle, lblWaiting, progress, btnCancel);
            rematchOverlay.getChildren().add(dialog);

            rematchOverlay.setOpacity(0);
            getChildren().add(rematchOverlay);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(500),
                    rematchOverlay);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            PauseTransition timeout = new PauseTransition(Duration.seconds(30));
            timeout.setOnFinished(e -> {
                if (rematchOverlay != null && getChildren().contains(rematchOverlay)) {
                    removeRematchOverlay();
                    showGameOverUI("DRAW", myScore, opponentScore);
                }
            });
            timeout.play();
        });
    }

    private void resetGame() {
        myScore = 0;
        opponentScore = 0;
        currentTarget = 0;
        numberLabelMap.clear();
        boardNumbers = null;

        if (lblLeftScore != null)
            lblLeftScore.setText("0");
        if (lblRightScore != null)
            lblRightScore.setText("0");
    }

    private void removeRematchOverlay() {
        if (rematchOverlay != null && getChildren().contains(rematchOverlay)) {
            getChildren().remove(rematchOverlay);
            rematchOverlay = null;
        }
    }

    private void removeGameOverOverlay() {
        if (gameOverOverlay != null && getChildren().contains(gameOverOverlay)) {
            getChildren().remove(gameOverOverlay);
            gameOverOverlay = null;
        }
    }

    private VBox createScoreBox(String label, int score, boolean isWinner) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        Label lblLabel = new Label(label);
        lblLabel.getStyleClass().add("game-over-score-label");
        Label lblScore = new Label(String.valueOf(score));
        lblScore.getStyleClass().add("game-over-score");
        if (isWinner) {
            lblScore.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 32px;");
        }
        box.getChildren().addAll(lblLabel, lblScore);
        return box;
    }

    private void showConfetti() {
        Pane confettiPane = new Pane();
        confettiPane.setMouseTransparent(true);
        confettiPane.setPrefSize(getWidth(), getHeight());

        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(8, 8);
            rect.setFill(javafx.scene.paint.Color.rgb(
                    rand.nextInt(255),
                    rand.nextInt(255),
                    rand.nextInt(255)));
            rect.setLayoutX(rand.nextDouble() * getWidth());
            rect.setLayoutY(-rand.nextDouble() * 200);

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    Duration.seconds(2 + rand.nextDouble() * 2), rect);
            tt.setToY(getHeight() + 200);
            tt.setToX(rect.getLayoutX() + (rand.nextDouble() - 0.5) * 200);
            tt.play();

            javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(
                    Duration.seconds(1), rect);
            rt.setByAngle(360);
            rt.setCycleCount(javafx.animation.Animation.INDEFINITE);
            rt.play();

            confettiPane.getChildren().add(rect);
        }

        getChildren().add(confettiPane);

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(e -> getChildren().remove(confettiPane));
        delay.play();
    }

    private VBox buildSkillPanel() {
        LanguageManager lang = LanguageManager.getInstance();

        VBox panel = new VBox(12);
        panel.setPrefWidth(380);
        panel.setMaxWidth(380);
        panel.setMaxHeight(200);
        panel.getStyleClass().add("player-info-panel");
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label(lang.getString("skill.title"));
        title.getStyleClass().add("player-info-title");

        Button btnClose = new Button("X");
        btnClose.getStyleClass().add("close-panel-button");
        btnClose.setOnAction(e -> hideSkillPanel());

        StackPane topBar = new StackPane(title, btnClose);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);

        VBox list = new VBox(10);
        list.setAlignment(Pos.CENTER_LEFT);

        int lightCount = PlayerSession.getLightSkill();
        int darkCount = PlayerSession.getDarkSkill();
        int freezeCount = PlayerSession.getFreezeSkill();
        System.out.println(
                "Building skill panel - Light: " + lightCount + ", Dark: " + darkCount + ", Freeze: " + freezeCount);

        if (lightCount > 0) {
            list.getChildren().add(createSkillRow(
                    lang.getString("skill.light"),
                    lightCount,
                    this::applyLightSkill));
        }

        if (darkCount > 0) {
            list.getChildren().add(createSkillRow(
                    lang.getString("skill.dark"),
                    darkCount,
                    this::applyDarkSkill));
        }

        if (freezeCount > 0) {
            list.getChildren().add(createSkillRow(
                    lang.getString("skill.freeze"),
                    freezeCount,
                    this::applyFreezeSkill));
        }

        if (list.getChildren().isEmpty()) {
            Label empty = new Label(lang.getString("skill.noSkill"));
            empty.getStyleClass().add("player-info-text");
            list.getChildren().add(empty);
        }

        panel.getChildren().addAll(topBar, list);
        return panel;
    }

    private HBox createSkillRow(String name, int count, Runnable action) {
        LanguageManager lang = LanguageManager.getInstance();

        Label lblName = new Label(name);
        lblName.getStyleClass().add("player-info-text");

        Label lblCount = new Label("x" + count);
        lblCount.getStyleClass().add("player-info-text");

        Button btnUse = new Button(lang.getString("skill.use"));
        btnUse.getStyleClass().add("profile-action-button");
        btnUse.setOnAction(e -> action.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, lblName, spacer, lblCount, btnUse);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void applyLightSkill() {
        if (!PlayerSession.useLightSkill()) {
            refreshSkillPanel();
            return;
        }

        hideSkillPanel();
        SoundManager.playSound("use_light_skill.mp3");

        Label targetLabel = numberLabelMap.get(currentTarget);
        if (targetLabel == null)
            return;

        targetLabel.getStyleClass().add("board-number-highlight");
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> targetLabel.getStyleClass().remove("board-number-highlight"));
        pause.play();
    }

    public void applyDarkSkill() {
        LanguageManager lang = LanguageManager.getInstance();
        if (!PlayerSession.useDarkSkill()) {
            // refreshSkillPanel();
            Toast.show(this, lang.getString("gameview.noskill.hidenumber"), 1500);
            return;
        }
        hideSkillPanel();
        SoundManager.playSound("use_dark_skill.mp3");
        gameClient.sendToServer("USE_DARK_SKILL");

        System.out.println("Da dung skill che man hinh doi thu");
        Toast.show(this, lang.getString("gameview.use.hidenumber"), 1500);
        // refreshSkillPanel();
    }

    private void applyFreezeSkill() {
        LanguageManager lang = LanguageManager.getInstance();
        if (!PlayerSession.useFreezeSkill()) {
            refreshSkillPanel();
            return;
        }
        hideSkillPanel();
        SoundManager.playSound("use_freeze_skill.mp3");
        gameClient.sendToServer("USE_FREEZE_SKILL");
        Toast.show(this, lang.getString("gameview.use.freeze"), 1500);
        refreshSkillPanel();
        System.out.println("Da dung skill dong bang doi thu");
    }

    private void toggleSkillPanel() {
        refreshSkillPanel();
        boolean isShowing = skillOverlay.isVisible();
        skillOverlay.setVisible(!isShowing);
        skillOverlay.setManaged(!isShowing);
    }

    private void hideSkillPanel() {
        skillOverlay.setVisible(false);
        skillOverlay.setManaged(false);
    }

    // public void refreshSkillPanel() {
    // skillOverlay.getChildren().clear();
    // skillPanel = buildSkillPanel();
    // skillOverlay.getChildren().add(skillPanel);
    // }

    public void refreshSkillPanel() {
        Platform.runLater(() -> {
            skillOverlay.getChildren().clear();
            skillPanel = buildSkillPanel();
            skillOverlay.getChildren().add(skillPanel);
        });
    }

    private List<Point2D> generateRandomPositions(int total, double boardWidth, double boardHeight) {
        List<Point2D> positions = new ArrayList<>();

        double topPadding = 85;
        double leftPadding = 55;
        double rightPadding = 55;
        double bottomPadding = 35;

        int cols = 10;
        int rows = 10;

        double usableWidth = boardWidth - leftPadding - rightPadding;
        double usableHeight = boardHeight - topPadding - bottomPadding;

        double cellWidth = usableWidth / cols;
        double cellHeight = usableHeight / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double baseX = leftPadding + (col * cellWidth);
                double baseY = topPadding + (row * cellHeight);
                double jitterX = RANDOM.nextDouble() * (cellWidth * 0.45);
                double jitterY = RANDOM.nextDouble() * (cellHeight * 0.45);
                positions.add(new Point2D(baseX + jitterX, baseY + jitterY));
            }
        }

        Collections.shuffle(positions);
        return positions.subList(0, Math.min(total, positions.size()));
    }

    private List<Integer> decodeNumberList(String numbersStr) {
        List<Integer> numbers = new ArrayList<>();
        String[] parts = numbersStr.split(",");
        for (String part : parts) {
            numbers.add(Integer.parseInt(part));
        }
        return numbers;
    }

    private Image loadImage(String path) {
        try {
            if (path == null || path.isEmpty()) {
                path = "/icon/Martin-Berube-Character-Devil.256.png";
            }
            return new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        } catch (Exception e) {
            return new Image(
                    Objects.requireNonNull(getClass().getResource("/icon/Martin-Berube-Character-Devil.256.png"))
                            .toExternalForm());
        }
    }

    @Override
    public void onWaiting() {
    }

    @Override
    public void onMatchFound(String opponentName, String opponentAvatar) {
    }

    @Override
    public void onBoardNumbers(String numbersStr) {
        Platform.runLater(() -> {
            boardNumbers = decodeNumberList(numbersStr);
            System.out.println("Received board numbers: " + boardNumbers.size() + " numbers");
            drawBoard();
        });
    }

    @Override
    public void onGoldUpdate(int goldAdded, int newGold) {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            Toast.show(this,
                    goldAdded + lang.getString("gameview.gold") + lang.getString("gameview.total") + ": " + newGold,
                    2000);
            PlayerSession.setGold(newGold);
        });
    }

    @Override
    public void onGameStart(int currentTarget, int duration) {
        Platform.runLater(() -> {
            updateTarget(currentTarget);
            timeRemaining = duration;
            startCountdown();
        });
    }

    @Override
    public void onTimeUpdate(int remainingSeconds) {
        Platform.runLater(() -> {
            timeRemaining = remainingSeconds;
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            if (lblTimer != null) {
                lblTimer.setText(String.format("%d:%02d", minutes, seconds));
            }
        });
    }

    @Override
    public void onRematchRejected(String rejecter) {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            removeRematchOverlay();
            removeGameOverOverlay();
            Toast.show(this, "❌ " + rejecter + lang.getString("gameview.refused"), 2000);

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> {
                if (getScene() != null) {
                    getScene().setRoot(new HomeView(playerName, avatarPath));
                }
            });
            delay.play();
        });
    }

    @Override
    public void onRematchCancelled() {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            removeRematchOverlay();
            removeGameOverOverlay();
            Toast.show(this, lang.getString("gameview.cancel"), 2000);

            if (getScene() != null) {
                getScene().setRoot(new HomeView(playerName, avatarPath));
            }
        });
    }

    @Override
    public void onCorrect(String playerName, int number, int score) {
        SoundManager.playSound("correct.mp3");

        Platform.runLater(() -> {
            updateScore(playerName, score);
            Label label = numberLabelMap.get(number);
            if (label != null) {
                label.getStyleClass().removeAll("board-number-correct", "board-number-opponent");

                if (playerName.equals(this.playerName)) {
                    label.getStyleClass().add("board-number-correct");
                    System.out.println("Marked number " + number + " as MY pick (green)");
                } else {
                    label.getStyleClass().add("board-number-opponent");
                    System.out.println("Marked number " + number + " as OPPONENT pick (purple)");
                }
                label.setDisable(true);
            }
        });
    }

    @Override
    public void onWrong(int number) {
        SoundManager.playSound("wrong.mp3");

        Platform.runLater(() -> {
            Label label = numberLabelMap.get(number);
            if (label != null) {
                label.getStyleClass().add("board-number-wrong");

                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(e -> {
                    label.getStyleClass().remove("board-number-wrong");
                });
                delay.play();

                System.out.println("Wrong number clicked: " + number);
            }
        });
    }

    @Override
    public void onNewTarget(int target) {
        Platform.runLater(() -> {
            updateTarget(target);
        });
    }

    @Override
    public void onGameOver(String winner, int yourScore, int opponentScore) {
        System.out.println("=== GAME_OVER RECEIVED ===");
        System.out.println("Winner: " + winner);
        System.out.println("Your score: " + yourScore);
        System.out.println("Opponent score: " + opponentScore);

        this.myScore = yourScore;
        this.opponentScore = opponentScore;

        Platform.runLater(() -> {
            showGameOverUI(winner, yourScore, opponentScore);
        });
    }

    @Override
    public void onGameEnd(String message) {
        Platform.runLater(() -> {
            if (timerTimeline != null) {
                timerTimeline.stop();
            }

            Toast.show(this, message, 2000);

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> {
                if (getScene() != null) {
                    getScene().setRoot(new HomeView(playerName, avatarPath));
                }
            });
            delay.play();
        });
    }

    @Override
    public void onRematchWaiting(String message) {
        Platform.runLater(() -> {
            System.out.println("Rematch waiting: " + message);
            if (rematchOverlay != null && waitingLabel != null) {
                waitingLabel.setText(message);
            }
        });
    }

    @Override
    public void onRematchAccepted() {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            removeRematchOverlay();
            removeGameOverOverlay();

            resetGame();
            Toast.show(this, "🎉" + lang.getString("gameview.both.agree"), 2000);
        });
    }

    @Override
    public void onRematchTimeout() {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            removeRematchOverlay();
            Toast.show(this, "⏰ " + lang.getString("gameview.timeup"), 2000);
            showGameOverUI("DRAW", myScore, opponentScore);
        });
    }

    @Override
    public void onRematchError(String error) {
        Platform.runLater(() -> {
            Toast.show(this, "❌ " + error, 2000);
        });
    }

    @Override
    public void onRematchRequest(String requester) {
        LanguageManager lang = LanguageManager.getInstance();
        System.out.println("=== REMATCH REQUEST RECEIVED in GameView from: " + requester + " ===");

        Platform.runLater(() -> {
            if (gameOverOverlay != null && getChildren().contains(gameOverOverlay)) {
                removeGameOverOverlay();
            }

            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle(lang.getString("gameview.recommend.play.again"));
            confirmDialog.setHeaderText(requester + lang.getString("gameview.wantto.playagain"));
            confirmDialog.setContentText(lang.getString("gameview.would.you.like.playagain"));

            DialogPane dialogPane = confirmDialog.getDialogPane();
            dialogPane.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");

            ButtonType yesButton = new ButtonType(lang.getString("gameview.agree"), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(lang.getString("gameview.disagree"), ButtonBar.ButtonData.NO);
            confirmDialog.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = confirmDialog.showAndWait();

            if (result.isPresent() && result.get() == yesButton) {
                System.out.println("User agreed to rematch");
                gameClient.requestRematch();
            } else {
                System.out.println("User rejected rematch");
                gameClient.sendToServer("REJECT_REMATCH");
            }
        });
    }

    @Override
    public void onLuckyEvent(int luckyNumber, int duration) {
        LanguageManager lang = LanguageManager.getInstance();

        Platform.runLater(() -> {
            Toast.show(this,
                    "🎲 " + lang.getString("gameview.lucky.number") + luckyNumber + "! "
                            + lang.getString("gameview.click.now")
                            + " " + duration
                            + lang.getString("gameview.double.points") + " 🎲",
                    3000);

            Label luckyLabel = numberLabelMap.get(luckyNumber);
            if (luckyLabel != null) {
                luckyLabel.getStyleClass().add("board-number-lucky-event");

                PauseTransition delay = new PauseTransition(Duration.seconds(duration));
                delay.setOnFinished(e -> {
                    luckyLabel.getStyleClass().remove("board-number-lucky-event");
                });
                delay.play();
            }
        });
    }

    @Override
    public void onLuckyEventEnd() {
        LanguageManager lang = LanguageManager.getInstance();

        Platform.runLater(() -> {
            Toast.show(this, "⏰ " + lang.getString("gameview.timeup.lucky.number"), 1500);
        });
    }

    @Override
    public void onLuckyBonus(String playerName, int number, String bonus) {
        LanguageManager lang = LanguageManager.getInstance();

        SoundManager.playSound("lucky.mp3");

        Platform.runLater(() -> {
            Toast.show(this, "🎉 " + playerName + lang.getString("gameview.found.lucky.number") + number + "! "
                    + bonus + lang.getString("gameview.bonus.points") + " 🎉",
                    2000);

            Label label = numberLabelMap.get(number);
            if (label != null) {
                label.getStyleClass().add("board-number-bonus");
                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(e -> label.getStyleClass().remove("board-number-bonus"));
                delay.play();
            }
        });
    }

    @Override
    public void onFreezePlayer(int duration) {
        LanguageManager lang = LanguageManager.getInstance();

        SoundManager.playSound("freeze_effect.mp3");

        Platform.runLater(() -> {
            isFrozen = true;
            for (Label label : numberLabelMap.values()) {
                label.setDisable(true);
            }
            Toast.show(this,
                    "❄️ " + lang.getString("gameview.frozen.in") + duration + lang.getString("gameview.second"), 2000);

            new Thread(() -> {
                try {
                    Thread.sleep(duration * 1000);
                    isFrozen = false;
                    for (Label label : numberLabelMap.values()) {
                        if (!label.getStyleClass().contains("board-number-correct") &&
                                !label.getStyleClass().contains("board-number-opponent")) {
                            label.setDisable(false);
                        }
                    }
                } catch (InterruptedException e) {
                }
            }).start();
        });
    }

    @Override
    public void onUnfreezePlayer() {
        LanguageManager lang = LanguageManager.getInstance();

        SoundManager.playSound("unfreeze.mp3");
        Platform.runLater(() -> {
            isFrozen = false;
            for (Label label : numberLabelMap.values()) {
                if (!label.getStyleClass().contains("board-number-correct") &&
                        !label.getStyleClass().contains("board-number-opponent")) {
                    label.setDisable(false);
                }
            }
            Toast.show(this, "❄️ " + lang.getString("gameview.frozen.continue"), 1000);
        });
    }

    @Override
    public void onBlockNumbers(int duration) {
        LanguageManager lang = LanguageManager.getInstance();

        SoundManager.playSound("block_effect.mp3");
        Platform.runLater(() -> {
            // for (Label label : numberLabelMap.values()) {
            // label.setVisible(false);
            // }

            for (Map.Entry<Integer, Label> entry : numberLabelMap.entrySet()) {
                Label label = entry.getValue();
                if (!label.getStyleClass().contains("board-number-correct") &&
                        !label.getStyleClass().contains("board-number-opponent")) {
                    label.setUserData(label.isVisible());
                    label.setVisible(false);
                }
            }

            blockOverlay = new StackPane();
            blockOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
            blockOverlay.setAlignment(Pos.CENTER);

            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);

            Label lblIcon = new Label("🔒");
            lblIcon.setStyle("-fx-font-size: 60px;");

            Label lblText = new Label(lang.getString("gameview.number.hidden"));
            lblText.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");

            Label lblDuration = new Label(duration + lang.getString("gameview.second"));
            lblDuration.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

            content.getChildren().addAll(lblIcon, lblText, lblDuration);
            blockOverlay.getChildren().add(content);

            blockOverlay.setOnMouseClicked(e -> {
            });

            getChildren().add(blockOverlay);
            Toast.show(this, lang.getString("gameview.oppent.number.hidden") + " " + duration
                    + lang.getString("gameview.second"), 2000);

            // Sau 3 giây, server sẽ gửi UNBLOCK_NUMBERS
        });
    }

    @Override
    public void onUnblockNumbers() {
        LanguageManager lang = LanguageManager.getInstance();

        SoundManager.playSound("unblock.mp3");
        Platform.runLater(() -> {

            if (blockOverlay != null) {
                getChildren().remove(blockOverlay);
                blockOverlay = null;
            }
            for (Map.Entry<Integer, Label> entry : numberLabelMap.entrySet()) {
                Label label = entry.getValue();
                if (!label.getStyleClass().contains("board-number-correct") &&
                        !label.getStyleClass().contains("board-number-opponent")) {
                    label.setVisible(true);
                } else {
                    label.setVisible(true);
                }
            }
            Toast.show(this, "🔓 " + lang.getString("gameview.number.displayed"), 1000);
        });
    }

    @Override
    public void onVideoRewardSuccess(int rewardAmount, int newGold) {
        LanguageManager lang = LanguageManager.getInstance();

        Platform.runLater(() -> {
            Toast.show(this,
                    "🎬 " + lang.getString("gameview.receive") + rewardAmount + lang.getString("gameview.gold") + "!"
                            + lang.getString("gameview.total") + ": " + newGold,
                    2000);
            PlayerSession.updateGold(newGold);
        });
    }

    @Override
    public void onVideoRewardFail(String message) {
        Platform.runLater(() -> {
            Toast.show(this, "❌ " + message, 2000);
        });
    }

    @Override
    public void onBuySkillSuccess(String skillType, int newCount, int newGold) {
        LanguageManager lang = LanguageManager.getInstance();

        Platform.runLater(() -> {
            PlayerSession.updateGold(newGold);

            switch (skillType) {
                case "light":
                    PlayerSession.setLightSkill(newCount);
                    break;
                case "dark":
                    PlayerSession.setDarkSkill(newCount);
                    break;
                case "freeze":
                    PlayerSession.setFreezeSkill(newCount);
                    break;
            }

            refreshSkillPanel();

            Toast.show(this,
                    lang.getString("gameview.buy") + " " + skillType + lang.getString("gameview.skill.success") + " ",
                    2000);
        });
    }

    public void showOverlay() {
        setVisible(true);
        setManaged(true);
    }

    public void hideOverlay() {
        setVisible(false);
        setManaged(false);
    }

}