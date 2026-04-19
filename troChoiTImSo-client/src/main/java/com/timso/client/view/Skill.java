package com.timso.client.view;

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

public class Skill extends StackPane {

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
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );

        getChildren().add(buildContent());
        refreshView();
    }

    private Node buildContent() {
    StackPane root = new StackPane();
    root.getStyleClass().add("skill-root");

    VBox content = new VBox(20);
    content.setAlignment(Pos.TOP_CENTER);
    content.setPadding(new Insets(18, 26, 24, 26));

    HBox topBar = new HBox();
    topBar.setAlignment(Pos.CENTER);

    Region leftSpacer = new Region();
    Region rightSpacer = new Region();
    HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
    HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

    Label title = new Label("Power");
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
            "Làm sáng số đúng",
            PlayerSession::getLightSkill,
            () -> PlayerSession.buyLightSkill()
    );

    VBox cardDark = createSkillCard(
            300,
            true,
            false,
            "Che màn hình đối thủ",
            PlayerSession::getDarkSkill,
            () -> PlayerSession.buyDarkSkill()
    );

    VBox cardFreeze = createSkillCard(
            500,
            true,
            true,
            "Đóng băng đối thủ",
            PlayerSession::getFreezeSkill,
            () -> PlayerSession.buyFreezeSkill()
    );

    cardRow.getChildren().addAll(cardLight, cardDark, cardFreeze);

    Button btnClaim = new Button("Nhận 1000");
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
            getClass().getResource("/images/302281_tiny.mp4")
    ).toExternalForm();

    Media media = new Media(videoUrl);
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    MediaView mediaView = new MediaView(mediaPlayer);

    mediaView.setPreserveRatio(true);
    mediaView.setFitWidth(720);
    mediaView.setFitHeight(405);

    Label lblWatching = new Label("Xem hết video để nhận 1000 vàng");
    lblWatching.getStyleClass().add("reward-text");

    VBox videoBox = new VBox(14, mediaView, lblWatching);
    videoBox.setAlignment(Pos.CENTER);
    videoBox.getStyleClass().add("reward-video-box");

    rewardOverlay.getChildren().clear();
    rewardOverlay.getChildren().add(videoBox);
    rewardOverlay.setVisible(true);
    rewardOverlay.setManaged(true);

    mediaPlayer.setOnEndOfMedia(() -> {
        PlayerSession.addGold(1000);
        refreshView();
        showMessage("Đã nhận 1000 vàng");
        mediaPlayer.stop();
        mediaPlayer.dispose();
        rewardOverlay.setVisible(false);
        rewardOverlay.setManaged(false);
        rewardOverlay.getChildren().clear();
    });

    mediaPlayer.setOnError(() -> {
        showMessage("Không thể phát video thưởng");
        mediaPlayer.dispose();
        rewardOverlay.setVisible(false);
        rewardOverlay.setManaged(false);
        rewardOverlay.getChildren().clear();
    });

    mediaPlayer.play();
}
    private VBox createSkillCard(
            int price,
            boolean darkPreview,
            boolean showFreezeText,
            String skillName,
            CountSupplier countSupplier,
            BuyAction buyAction
    ) {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("skill-card");

        StackPane previewWrapper = new StackPane();
        previewWrapper.getStyleClass().add(darkPreview ? "skill-preview-dark" : "skill-preview-light");

        Pane previewNumbers = buildPreviewNumbers();
        previewWrapper.getChildren().add(previewNumbers);

        if (showFreezeText) {
            Label freezeText = new Label("You are frozen");
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

        Button btnBuy = new Button("Mua");
        btnBuy.getStyleClass().addAll("action-button", "skill-buy-button");
        btnBuy.setOnAction(e -> {
            boolean bought = buyAction.buy();
            if (bought) {
                showMessage("Mua thành công: " + skillName);
            } else {
                showMessage("Không đủ vàng để mua");
            }
            refreshView();
        });

        VBox bottomBox = new VBox(8, priceRow, lblSkillName, btnBuy);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getStyleClass().add("skill-card-bottom");

        card.getChildren().addAll(previewWrapper, bottomBox);

        card.sceneProperty().addListener((obs, oldScene, newScene) ->
                lblCount.setText("x" + countSupplier.getCount()));
        card.parentProperty().addListener((obs, oldParent, newParent) ->
                lblCount.setText("x" + countSupplier.getCount()));

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
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm()
        );
    }

    @FunctionalInterface
    private interface BuyAction {
        boolean buy();
    }

    @FunctionalInterface
    private interface CountSupplier {
        int getCount();
    }
}