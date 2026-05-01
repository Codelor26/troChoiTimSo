package com.timso.client.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;

import java.util.Objects;

import com.timso.client.network.GameClient;

public class Skill extends StackPane implements GameClient.GameListener {

    private final String playerName;
    private final String avatarPath;

    private final Label lblGold = new Label();
    private final Label lblMessage = new Label("");
    private StackPane rewardOverlay;

    public Skill(String playerName, String avatarPath) {
        this.playerName = playerName;
        this.avatarPath = avatarPath;

        getStyleClass().add("auth-root");
        getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        getChildren().add(buildContent());
        refreshView();

        GameClient.getInstance().setListener(this);

    }

    private Node buildContent() {
        LanguageManager lang = LanguageManager.getInstance();

        StackPane root = new StackPane();
        root.getStyleClass().add("skill-root");

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 26, 24, 26));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        Label title = new Label(lang.getString("skill.title"));
        title.getStyleClass().add("skill-title");

        HBox goldBox = new HBox(6);
        goldBox.setAlignment(Pos.CENTER_RIGHT);

        lblGold.getStyleClass().add("skill-price-text");
        ImageView topCoin = createCoinIcon(30);
        goldBox.getChildren().addAll(lblGold, topCoin);

        topBar.getChildren().addAll(leftSpacer, title, rightSpacer, goldBox);

        HBox cardRow = new HBox(34);
        cardRow.setAlignment(Pos.CENTER);

        VBox cardLight = createSkillCard(
                200,
                false,
                false,
                LanguageManager.getInstance().getString("skill.light"),
                PlayerSession::getLightSkill,
                () -> PlayerSession.buyLightSkill());

        VBox cardDark = createSkillCard(
                300,
                true,
                false,
                LanguageManager.getInstance().getString("skill.dark"),
                PlayerSession::getDarkSkill,
                () -> PlayerSession.buyDarkSkill());

        VBox cardFreeze = createSkillCard(
                500,
                true,
                true,
                LanguageManager.getInstance().getString("skill.freeze"),
                PlayerSession::getFreezeSkill,
                () -> PlayerSession.buyFreezeSkill());

        cardRow.getChildren().addAll(cardLight, cardDark, cardFreeze);

        Button btnClaim = new Button(LanguageManager.getInstance().getString("skill.btn.claim.1000"));
        btnClaim.setGraphic(createCoinIcon(36));
        btnClaim.setContentDisplay(ContentDisplay.RIGHT);
        btnClaim.getStyleClass().addAll("action-button", "skill-claim-button");
        btnClaim.setOnAction(e -> showRewardVideo());

        lblMessage.getStyleClass().add("setting-error-label");
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);

        Button btnBack = new Button("←");
        btnBack.getStyleClass().add("skill-back-button");
        btnBack.setOnAction(e -> {
            GameClient.getInstance().setListener(null);
            if (getScene() != null) {
                getScene().setRoot(new HomeView(playerName, avatarPath));
            }
        });

        rewardOverlay = new StackPane();
        rewardOverlay.getStyleClass().add("reward-overlay");
        rewardOverlay.setVisible(false);
        rewardOverlay.setManaged(false);

        root.getChildren().addAll(content, btnBack, rewardOverlay);
        content.getChildren().addAll(topBar, cardRow, btnClaim, lblMessage);

        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(18, 0, 0, 18));

        return root;
    }

    private void showRewardVideo() {
        String videoUrl = Objects.requireNonNull(
                getClass().getResource("/images/302281_tiny.mp4")).toExternalForm();

        Media media = new Media(videoUrl);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(720);
        mediaView.setFitHeight(405);

        Label lblWatching = new Label(LanguageManager.getInstance().getString("skill.video.watch"));
        lblWatching.getStyleClass().add("reward-text");

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().add("video-close-button");
        btnClose.setOnAction(e -> closeVideo(false));

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(btnClose);

        VBox videoBox = new VBox(14, topBar, mediaView, lblWatching);
        videoBox.setAlignment(Pos.CENTER);
        videoBox.getStyleClass().add("reward-video-box");

        rewardOverlay.getChildren().clear();
        rewardOverlay.getChildren().add(videoBox);
        rewardOverlay.setVisible(true);
        rewardOverlay.setManaged(true);

        mediaPlayer.setOnEndOfMedia(() -> {
            GameClient.getInstance().sendToServer("CLAIM_VIDEO_REWARD");

            showMessage(LanguageManager.getInstance().getString("skill.video.success"));
            mediaPlayer.stop();
            mediaPlayer.dispose();
            rewardOverlay.setVisible(false);
            rewardOverlay.setManaged(false);
            rewardOverlay.getChildren().clear();
        });

        mediaPlayer.setOnError(() -> {
            showMessage(LanguageManager.getInstance().getString("skill.video.fail"));
            mediaPlayer.dispose();
            rewardOverlay.setVisible(false);
            rewardOverlay.setManaged(false);
            rewardOverlay.getChildren().clear();
        });

        mediaPlayer.play();
    }

    private void closeVideo(boolean reward) {
        LanguageManager lang = LanguageManager.getInstance();
        rewardOverlay.setVisible(false);
        rewardOverlay.setManaged(false);
        rewardOverlay.getChildren().clear();

        if (!reward) {
            showMessage(lang.getString("skill.video.off"));
        }
    }

    private VBox createSkillCard(
            int price,
            boolean darkPreview,
            boolean showFreezeText,
            String skillName,
            CountSupplier countSupplier,
            BuyAction buyAction) {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("skill-card");

        StackPane previewWrapper = new StackPane();
        previewWrapper.getStyleClass().add(darkPreview ? "skill-preview-dark" : "skill-preview-light");

        Pane previewNumbers = buildPreviewNumbers();
        previewWrapper.getChildren().add(previewNumbers);

        if (showFreezeText) {
            Label freezeText = new Label(LanguageManager.getInstance().getString("skill.lbl.frozen"));
            freezeText.getStyleClass().add("skill-freeze-text");
            previewWrapper.getChildren().add(freezeText);
        }

        Label lblCount = new Label();
        lblCount.getStyleClass().add("player-info-text");
        StackPane.setAlignment(lblCount, Pos.TOP_RIGHT);
        StackPane.setMargin(lblCount, new Insets(8, 10, 0, 0));
        previewWrapper.getChildren().add(lblCount);

        HBox priceRow = new HBox(6);
        priceRow.setAlignment(Pos.CENTER);
        priceRow.getStyleClass().add("skill-price-row");

        Label lblPrice = new Label(String.valueOf(price));
        lblPrice.getStyleClass().add("skill-price-text");

        ImageView coinIcon = createCoinIcon(30);
        priceRow.getChildren().addAll(lblPrice, coinIcon);

        Label lblSkillName = new Label(skillName);
        lblSkillName.getStyleClass().add("setting-feedback-text");
        lblSkillName.setWrapText(true);
        lblSkillName.setMaxWidth(150);
        lblSkillName.setAlignment(Pos.CENTER);

        Button btnBuy = new Button(LanguageManager.getInstance().getString("skill.btn.buy"));
        btnBuy.getStyleClass().addAll("action-button", "skill-buy-button");
        btnBuy.setOnAction(e -> {
            System.out.println("=== BUYING SKILL ===");
            System.out.println("Current gold in session: " + PlayerSession.getGold());
            boolean bought = buyAction.buy();
            if (bought) {
                showMessage(LanguageManager.getInstance().getString("skill.msg.buy.success") + skillName);
            } else {
                showMessage(LanguageManager.getInstance().getString("skill.msg.buy.fail"));
            }
            refreshView();
        });

        VBox bottomBox = new VBox(8, priceRow, lblSkillName, btnBuy);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getStyleClass().add("skill-card-bottom");

        card.getChildren().addAll(previewWrapper, bottomBox);

        card.sceneProperty().addListener((obs, oldScene, newScene) -> lblCount.setText("x" + countSupplier.getCount()));
        card.parentProperty()
                .addListener((obs, oldParent, newParent) -> lblCount.setText("x" + countSupplier.getCount()));

        card.setUserData(lblCount);

        return card;
    }

    private Pane buildPreviewNumbers() {
        Pane pane = new Pane();
        pane.setPrefSize(180, 150);

        pane.getChildren().add(createPreviewLabel("12", 20, 24, "skill-preview-number"));
        pane.getChildren().add(createPreviewLabel("11", 125, 24, "skill-preview-number"));
        pane.getChildren().add(createPreviewLabel("15", 22, 102, "skill-preview-number"));
        pane.getChildren().add(createPreviewLabel("13", 86, 102, "skill-preview-number"));
        pane.getChildren().add(createPreviewLabel("16", 136, 72, "skill-preview-number"));
        pane.getChildren().add(createPreviewLabel("14", 62, 58, "skill-preview-main-number"));

        return pane;
    }

    private Label createPreviewLabel(String text, double x, double y, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private ImageView createCoinIcon(double size) {
        ImageView coinIcon = new ImageView(loadImage("/icon/Hopstarter-Soft-Scraps-Coin.256.png"));
        coinIcon.setFitWidth(size);
        coinIcon.setFitHeight(size);
        coinIcon.setPreserveRatio(true);
        return coinIcon;
    }

    private void refreshView() {
        lblGold.setText(String.valueOf(PlayerSession.getGold()));

        updateCardCount(0, PlayerSession.getLightSkill());
        updateCardCount(1, PlayerSession.getDarkSkill());
        updateCardCount(2, PlayerSession.getFreezeSkill());
    }

    private void updateCardCount(int cardIndex, int count) {
        VBox content = (VBox) ((StackPane) getChildren().get(0)).getChildren().get(0);
        HBox cardRow = (HBox) content.getChildren().get(1);
        VBox card = (VBox) cardRow.getChildren().get(cardIndex);
        Label lblCount = (Label) card.getUserData();
        if (lblCount != null) {
            lblCount.setText("x" + count);
        }
    }

    private void showMessage(String message) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private Image loadImage(String path) {
        return new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
    }

    @FunctionalInterface
    private interface BuyAction {
        boolean buy();
    }

    @FunctionalInterface
    private interface CountSupplier {
        int getCount();
    }

    @Override
    public void onBuySkillSuccess(String skillType, int newCount, int newGold) {
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

            refreshView();
            LanguageManager lang = LanguageManager.getInstance();
            showMessage(lang.getString("skill.buy.success") + " +1 " + skillType + " skill");
        });
    }

    @Override
    public void onVideoRewardSuccess(int rewardAmount, int newGold) {
        LanguageManager lang = LanguageManager.getInstance();
        Platform.runLater(() -> {
            PlayerSession.updateGold(newGold);
            refreshView();
            showMessage("🎉 " + lang.getString("gameview.receive") + rewardAmount + "  " + lang.getString("home.gold")
                    + lang.getString("gameview.total") + ": " + newGold);
        });
    }

    @Override
    public void onVideoRewardFail(String message) {
        Platform.runLater(() -> {
            showMessage("❌ " + message);
        });
    }

    @Override
    public void onWaiting() {
    }

    @Override
    public void onMatchFound(String opponentName, String opponentAvatar) {
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
    public void onGoldUpdate(int goldAdded, int newGold) {
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
    public void onLeaderboard(String data) {
        // Không cần xử lý ở Skill
    }

    @Override
    public void onMyRank(int rank) {
        // Không cần xử lý ở Skill
    }

}