package com.timso.client.view;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.timso.client.network.GameClient;

public class LeaderboardView extends StackPane implements GameClient.GameListener {

    private final String playerName;
    private final String avatarPath;
    private GameClient gameClient;
    private VBox content;
    private Label lblMyRank;
    private VBox listContainer;
    private ScrollPane scrollPane;
    private TextField searchField;
    private Button btnSearch;

    private List<Map<String, Object>> tempLeaderboard = new ArrayList<>();

    public LeaderboardView(String playerName, String avatarPath) {
        this.playerName = playerName;
        this.avatarPath = avatarPath;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        gameClient = GameClient.getInstance();
        gameClient.setListener(this);

        getChildren().add(buildContent());

        // gameClient.sendToServer("GET_LEADERBOARD");
        // gameClient.sendToServer("GET_MY_RANK");
        gameClient.sendToServer("GET_LEADERBOARD");

    }

    private Node buildContent() {
        LanguageManager lang = LanguageManager.getInstance();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("leaderboard-root");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.getStyleClass().add("home-header");

        Button btnBack = new Button("←");
        btnBack.getStyleClass().add("game-back-button");
        btnBack.setOnAction(e -> {
            gameClient.setListener(null);
            if (getScene() != null) {
                getScene().setRoot(new HomeView(playerName, avatarPath));
            }
        });

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Label title = new Label("");
        title.getStyleClass().add("leaderboard-title");
        title.setFont(Font.font(28));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_RIGHT);

        searchField = new TextField();
        searchField.setPromptText(lang.getString("boardView.search"));
        searchField.setPrefWidth(180);
        searchField.getStyleClass().add("search-field");

        btnSearch = new Button("🔍");
        btnSearch.getStyleClass().add("search-button");
        btnSearch.setOnAction(e -> searchPlayer());
        searchField.setOnAction(e -> searchPlayer());

        searchBox.getChildren().addAll(searchField, btnSearch);

        header.getChildren().addAll(btnBack, leftSpacer, title, rightSpacer,
                searchBox);
        // header.getChildren().addAll(btnBack, leftSpacer, title, searchBox);

        content = new VBox(15);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        VBox myRankBox = new VBox(5);
        myRankBox.setAlignment(Pos.CENTER);
        myRankBox.getStyleClass().add("leaderboard-my-rank");

        Label lblMyRankTitle = new Label(lang.getString("boardView.rank"));
        lblMyRankTitle.getStyleClass().add("leaderboard-my-rank-title");

        lblMyRank = new Label("--");
        lblMyRank.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");

        myRankBox.getChildren().addAll(lblMyRankTitle, lblMyRank);

        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER);
        tableHeader.setPadding(new Insets(10));
        tableHeader.getStyleClass().add("leaderboard-header");

        String[] headers = { lang.getString("boardView.rate"), lang.getString("boardView.user"),
                lang.getString("boardView.match"),
                lang.getString("boardView.win"),
                lang.getString("boardView.lose"), lang.getString("boardView.draw"),
                lang.getString("boardView.point"),
                lang.getString("boardView.ratio") };
        int[] widths = { 50, 250, 60, 60, 60, 60, 70, 70 };

        for (int i = 0; i < headers.length; i++) {
            Label lbl = new Label(headers[i]);
            lbl.setPrefWidth(widths[i]);
            lbl.setAlignment(Pos.CENTER);
            lbl.getStyleClass().add("leaderboard-header-text");
            tableHeader.getChildren().add(lbl);
        }

        listContainer = new VBox(5);
        scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.getStyleClass().add("leaderboard-scroll");

        content.getChildren().addAll(myRankBox, tableHeader, scrollPane);

        root.setTop(header);
        root.setCenter(content);

        return root;
    }

    private HBox createLeaderboardRow(int rank, Map<String, Object> player) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.getStyleClass().add("leaderboard-row");

        // Rank
        Label lblRank = new Label(String.valueOf(rank));
        lblRank.setPrefWidth(50);
        lblRank.setAlignment(Pos.CENTER);
        lblRank.getStyleClass().add("rank-text");
        if (rank == 1)
            lblRank.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        else if (rank == 2)
            lblRank.setStyle("-fx-text-fill: #C0C0C0; -fx-font-weight: bold;");
        else if (rank == 3)
            lblRank.setStyle("-fx-text-fill: #CD7F32; -fx-font-weight: bold;");

        // Player info
        HBox playerBox = new HBox(8);
        playerBox.setAlignment(Pos.CENTER_LEFT);
        playerBox.setPrefWidth(250);

        ImageView avatar = new ImageView(loadImage((String) player.get("avatar")));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);

        Label lblName = new Label((String) player.get("playerName"));
        lblName.getStyleClass().add("leaderboard-player-name");

        playerBox.getChildren().addAll(avatar, lblName);

        // Stats
        Label lblGames = new Label(String.valueOf(player.get("totalGames")));
        lblGames.setPrefWidth(60);
        lblGames.setAlignment(Pos.CENTER);
        lblGames.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");

        Label lblWins = new Label(String.valueOf(player.get("wins")));
        lblWins.setPrefWidth(60);
        lblWins.setAlignment(Pos.CENTER);
        lblWins.setStyle("-fx-text-fill: #4CAF50;");

        Label lblLosses = new Label(String.valueOf(player.get("losses")));
        lblLosses.setPrefWidth(60);
        lblLosses.setAlignment(Pos.CENTER);
        lblLosses.setStyle("-fx-text-fill: #ff6b6b;");

        Label lblDraws = new Label(String.valueOf(player.get("draws")));
        lblDraws.setPrefWidth(60);
        lblDraws.setAlignment(Pos.CENTER);
        lblDraws.setStyle("-fx-text-fill: #ffd32a; -fx-font-size: 13px;");

        Label lblPoints = new Label(String.valueOf(player.get("totalPoints")));
        lblPoints.setPrefWidth(70);
        lblPoints.setAlignment(Pos.CENTER);
        lblPoints.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");

        Label lblWinRate = new Label(String.valueOf(player.get("winRate")) + "%");
        lblWinRate.setPrefWidth(70);
        lblWinRate.setAlignment(Pos.CENTER);
        lblWinRate.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-font-size: 14px;");

        row.getChildren().addAll(lblRank, playerBox, lblGames, lblWins, lblLosses, lblDraws, lblPoints, lblWinRate);

        return row;
    }

    public void addLeaderboardPlayer(String playerName, String avatar, int totalGames, int wins, int losses, int draws,
            int totalPoints, double winRate) {
        Map<String, Object> player = new HashMap<>();
        player.put("playerName", playerName);
        player.put("avatar", avatar);
        player.put("totalGames", totalGames);
        player.put("wins", wins);
        player.put("losses", losses);
        player.put("draws", draws);
        player.put("totalPoints", totalPoints);
        player.put("winRate", winRate);
        tempLeaderboard.add(player);
        System.out.println("Added player to temp: " + playerName);
    }

    public void finishLeaderboard() {
        LanguageManager lang = LanguageManager.getInstance();
        System.out.println("Finishing leaderboard, total players: " + tempLeaderboard.size());
        Platform.runLater(() -> {
            listContainer.getChildren().clear();

            if (tempLeaderboard.isEmpty()) {
                Label noData = new Label(lang.getString("boardView.no.data"));
                noData.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                listContainer.getChildren().add(noData);
                lblMyRank.setText(lang.getString("boardView.no.data"));
                return;
            }

            int rank = 1;
            int userRank = -1;

            String currentPlayer = PlayerSession.getCurrentUser() != null
                    ? PlayerSession.getCurrentUser().getPlayerName()
                    : playerName;

            System.out.println("Current player: " + currentPlayer);

            for (Map<String, Object> player : tempLeaderboard) {
                String playerNameInList = (String) player.get("playerName");
                System.out.println("Player in list: " + playerNameInList);

                if (playerNameInList.equals(currentPlayer)) {
                    userRank = rank;
                    System.out.println("Found user at rank: " + rank);
                }
                listContainer.getChildren().add(createLeaderboardRow(rank++, player));
            }

            System.out.println("Total rows added: " + (rank - 1));
            System.out.println("User rank: " + userRank);

            if (userRank > 0) {
                lblMyRank.setText("#" + userRank);
            } else {
                lblMyRank.setText(lang.getString("boardView.no.data"));
            }

            tempLeaderboard.clear();
        });
    }

    public void onLeaderboardEmpty() {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            listContainer.getChildren().clear();
            Label noData = new Label(lang.getString("boardView.no.data"));
            noData.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            listContainer.getChildren().add(noData);
        });
    }

    private void searchPlayer() {
        LanguageManager lang = LanguageManager.getInstance();
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            refreshLeaderboard();
            return;
        }

        for (Node node : listContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (Node child : row.getChildren()) {
                    if (child instanceof HBox) {
                        HBox playerBox = (HBox) child;
                        for (Node inner : playerBox.getChildren()) {
                            if (inner instanceof Label) {
                                Label nameLabel = (Label) inner;
                                if (nameLabel.getText().toLowerCase().contains(keyword)) {
                                    scrollPane.setVvalue(row.getLayoutY() / listContainer.getHeight());
                                    row.setStyle("-fx-background-color: #ff6b6b; -fx-background-radius: 8;");
                                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                                    delay.setOnFinished(e -> row.setStyle(""));
                                    delay.play();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        Toast.show(this, lang.getString("boardView.no.player") + ": " + keyword, 2000);
    }

    private void refreshLeaderboard() {
        tempLeaderboard.clear();
        gameClient.sendToServer("GET_LEADERBOARD");
    }

    @Override
    public void onLeaderboard(String data) {
        System.out.println("onLeaderboard (legacy) received: "
                + (data != null ? data.substring(0, Math.min(100, data.length())) : "null"));
    }

    @Override
    public void onBoardNumbers(String numbersStr) {
    }

    @Override
    public void onGameStart(int currentTarget, int duration) {
    }

    @Override
    public void onTimeUpdate(int remainingSeconds) {
    }

    @Override
    public void onCorrect(String playerName, int number, int score) {
    }

    @Override
    public void onWrong(int number) {
    }

    @Override
    public void onNewTarget(int target) {
    }

    @Override
    public void onGameOver(String winner, int yourScore, int opponentScore) {
    }

    @Override
    public void onGameEnd(String message) {
    }

    @Override
    public void onRematchRequest(String requester) {
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
    public void onRematchRejected(String rejecter) {
    }

    @Override
    public void onRematchCancelled() {
    }

    @Override
    public void onRematchWaiting(String message) {
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

    @Override
    public void onBuySkillSuccess(String skillType, int newCount, int newGold) {
    }

    @Override
    public void onWaiting() {
    }

    @Override
    public void onMatchFound(String opponentName, String opponentAvatar) {
    }

    @Override
    public void onGoldUpdate(int goldAdded, int newGold) {
    }

    @Override
    public void onMyRank(int rank) {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            if (rank == 0) {
                lblMyRank.setText(lang.getString("boardView.no.data"));
            } else {
                lblMyRank.setText("#" + rank);
            }
        });
    }

    @Override
    public void onStatsResponse(int onlineCount, int totalUsers) {
    }

    private String decode(String value) {
        try {
            return new String(java.util.Base64.getDecoder().decode(value), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
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
}