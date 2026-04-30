package com.timso.client.view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Objects;

public class SoundManager {
    private static MediaPlayer backgroundPlayer;
    private static boolean musicEnabled = true;
    private static boolean effectEnabled = true;

    public static void playBackgroundMusic() {
        if (!musicEnabled)
            return;

        String musicFile = Objects.requireNonNull(
                SoundManager.class.getResource("/sounds/background.mp3")).toExternalForm();

        Media media = new Media(musicFile);
        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setVolume(0.5); //// Âm lượng 50%
        backgroundPlayer.play();
    }

    public static void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
            backgroundPlayer = null;
        }
    }

    public static void playSound(String soundFile) {
        if (!effectEnabled)
            return;

        try {
            String file = Objects.requireNonNull(
                    SoundManager.class.getResource("/sounds/" + soundFile)).toExternalForm();

            Media media = new Media(file);
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(0.7);
            player.play();

            player.setOnEndOfMedia(() -> player.dispose());
        } catch (Exception e) {
            System.err.println("Cannot play sound: " + soundFile);
        }
    }

    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (enabled) {
            playBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
    }

    public static void setEffectEnabled(boolean enabled) {
        effectEnabled = enabled;
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static boolean isEffectEnabled() {
        return effectEnabled;
    }
}