package com.timso.client.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import java.util.Random;

public class GameView extends StackPane {

    private static final Random RANDOM = new Random();

    private final String playerName;
    private final String avatarPath;
    private final int targetNumber;

    private final Map<Integer, Label> numberLabelMap = new HashMap<>();

    private Pane boardPane;
    private StackPane skillOverlay;
    private VBox skillPanel;

    public GameView(String playerName, String avatarPath) {
        this.playerName = playerName;
        this.avatarPath = avatarPath;
        this.targetNumber = RANDOM.nextInt(100) + 1;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        getChildren().add(buildContent());
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

        Label lblLeft = new Label(playerName + " 9");
        lblLeft.getStyleClass().add("game-player-label");

        leftBox.getChildren().addAll(leftAvatar, lblLeft);

        VBox centerBox = new VBox(2);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setFillWidth(false);

        Label lblTarget = new Label(String.valueOf(targetNumber));
        lblTarget.getStyleClass().add("target-number-label");

        StackPane targetCircle = new StackPane(lblTarget);
        targetCircle.getStyleClass().add("target-circle");
        targetCircle.setMinSize(56, 56);
        targetCircle.setPrefSize(56, 56);
        targetCircle.setMaxSize(56, 56);

        Label lblTimer = new Label("2:00");
        lblTimer.getStyleClass().add("game-timer-label");

        centerBox.getChildren().addAll(targetCircle, lblTimer);

        HBox rightBox = new HBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblRight = new Label("10 " + playerName);
        lblRight.getStyleClass().add("game-player-label");

        ImageView rightAvatar = new ImageView(loadImage(avatarPath));
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

        rightBox.getChildren().addAll(lblRight, rightAvatar, btnMySkill);

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
            if (getScene() != null) {
                getScene().setRoot(new HomeView(playerName, avatarPath));
            }
        });

        boardPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (boardPane.getWidth() > 0 && boardPane.getHeight() > 0) {
                populateBoardNumbers(btnBack);
            }
        });

        boardPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (boardPane.getWidth() > 0 && boardPane.getHeight() > 0) {
                populateBoardNumbers(btnBack);
            }
        });

        boardArea.getChildren().add(boardPane);
        return boardArea;
    }

    private void populateBoardNumbers(Button btnBack) {
        boardPane.getChildren().clear();
        numberLabelMap.clear();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numbers.add(i);
        }

        List<Point2D> positions = generateRandomPositions(100, boardPane.getWidth(), boardPane.getHeight());

        for (int i = 0; i < numbers.size(); i++) {
            int number = numbers.get(i);

            Label lbl = new Label(String.valueOf(number));
            lbl.getStyleClass().add("board-number");
            lbl.setCursor(Cursor.HAND);

            Point2D point = positions.get(i);
            lbl.setLayoutX(point.getX());
            lbl.setLayoutY(point.getY());

            lbl.setOnMouseClicked(e -> handleNumberClick(number, lbl));

            numberLabelMap.put(number, lbl);
            boardPane.getChildren().add(lbl);
        }

        btnBack.setLayoutX(16);
        btnBack.setLayoutY(16);
        boardPane.getChildren().add(btnBack);
    }

    private void handleNumberClick(int number, Label label) {
        if (label.getStyleClass().contains("board-number-correct")) {
            return;
        }

        if (number == targetNumber) {
            if (!label.getStyleClass().contains("board-number-correct")) {
                label.getStyleClass().add("board-number-correct");
            }
            System.out.println("Ban da chon dung so: " + number);
        } else {
            if (!label.getStyleClass().contains("board-number-wrong")) {
                label.getStyleClass().add("board-number-wrong");
            }

            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> label.getStyleClass().remove("board-number-wrong"));
            pause.play();

            System.out.println("Ban da chon sai so: " + number);
        }
    }

    private VBox buildSkillPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(380);
        panel.setMaxWidth(380);
        panel.getStyleClass().add("player-info-panel");
        panel.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Kỹ năng");
        title.getStyleClass().add("player-info-title");

        Button btnClose = new Button("X");
        btnClose.getStyleClass().add("close-panel-button");
        btnClose.setOnAction(e -> hideSkillPanel());

        StackPane topBar = new StackPane(title, btnClose);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);

        VBox list = new VBox(10);
        list.setAlignment(Pos.CENTER_LEFT);

        if (PlayerSession.getLightSkill() > 0) {
            list.getChildren().add(createSkillRow(
                    "Làm sáng số đúng",
                    PlayerSession.getLightSkill(),
                    this::applyLightSkill));
        }

        if (PlayerSession.getDarkSkill() > 0) {
            list.getChildren().add(createSkillRow(
                    "Che màn hình đối thủ",
                    PlayerSession.getDarkSkill(),
                    this::applyDarkSkill));
        }

        if (PlayerSession.getFreezeSkill() > 0) {
            list.getChildren().add(createSkillRow(
                    "Đóng băng đối thủ",
                    PlayerSession.getFreezeSkill(),
                    this::applyFreezeSkill));
        }

        if (list.getChildren().isEmpty()) {
            Label empty = new Label("Bạn chưa mua kỹ năng nào");
            empty.getStyleClass().add("player-info-text");
            list.getChildren().add(empty);
        }

        panel.getChildren().addAll(topBar, list);
        return panel;
    }

    private HBox createSkillRow(String name, int count, Runnable action) {
        Label lblName = new Label(name);
        lblName.getStyleClass().add("player-info-text");

        Label lblCount = new Label("x" + count);
        lblCount.getStyleClass().add("player-info-text");

        Button btnUse = new Button("Dùng");
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

        Label targetLabel = numberLabelMap.get(targetNumber);
        if (targetLabel == null) {
            return;
        }

        if (!targetLabel.getStyleClass().contains("board-number-highlight")) {
            targetLabel.getStyleClass().add("board-number-highlight");
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> targetLabel.getStyleClass().remove("board-number-highlight"));
        pause.play();
    }

    private void applyDarkSkill() {
        if (!PlayerSession.useDarkSkill()) {
            refreshSkillPanel();
            return;
        }

        hideSkillPanel();
        System.out.println("Da dung skill che man hinh doi thu");
    }

    private void applyFreezeSkill() {
        if (!PlayerSession.useFreezeSkill()) {
            refreshSkillPanel();
            return;
        }

        hideSkillPanel();
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

    private void refreshSkillPanel() {
        skillOverlay.getChildren().clear();
        skillPanel = buildSkillPanel();
        skillOverlay.getChildren().add(skillPanel);
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
        return positions.subList(0, total);
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
    }

    // private Image loadImage(String path) {
    // try {
    // if (path.startsWith("http")) {
    // return new Image(path, true);
    // }

    // if (path.matches("^[a-zA-Z]:\\\\.*")) {
    // String fixedPath = "file:///" + path.replace("\\", "/");
    // return new Image(fixedPath, true);
    // }

    // var url = getClass().getResource(path);
    // if (url != null) {
    // return new Image(url.toExternalForm(), true);
    // }

    // System.out.println("Không tìm thấy ảnh: " + path);
    // return new Image("https://via.placeholder.com/42");

    // } catch (Exception e) {
    // System.out.println("Lỗi load ảnh: " + path);
    // e.printStackTrace();
    // return new Image("https://via.placeholder.com/42");
    // }
    // }

    public void showOverlay() {
        setVisible(true);
        setManaged(true);
    }

    public void hideOverlay() {
        setVisible(false);
        setManaged(false);
    }
}