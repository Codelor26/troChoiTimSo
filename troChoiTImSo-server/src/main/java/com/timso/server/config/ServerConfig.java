package com.timso.server.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    private int boardSize;
    private int gameTime;
    private int maxPlayers;
    private int winGoldReward;
    private int luckyNumberBonus;

    private boolean enableLuckyNumbers;
    private int luckyNumberMultiplier;
    private int luckyEventTime;
    private int luckyEventDurationTime;

    private int skillLightPrice;
    private int skillDarkPrice;
    private int skillFreezePrice;

    private boolean videoRewardEnabled;
    private int videoRewardAmount;
    private int videoDailyLimit;
    private String videoUrl;

    public ServerConfig() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                prop.load(fis);
            } catch (IOException e) {
                System.out.println("Không tìm thấy config.properties, tạo file mặc định");
                createDefaultConfig();
                try (FileInputStream fis = new FileInputStream("config.properties")) {
                    prop.load(fis);
                }
            }

            boardSize = Integer.parseInt(prop.getProperty("board.size", "100"));
            gameTime = Integer.parseInt(prop.getProperty("game.time", "120"));
            maxPlayers = Integer.parseInt(prop.getProperty("max.players", "2"));

            winGoldReward = Integer.parseInt(prop.getProperty("win.gold.reward", "100"));
            luckyNumberBonus = Integer.parseInt(prop.getProperty("lucky.number.bonus", "50"));
            luckyEventTime = Integer.parseInt(prop.getProperty("lucky.event.time", "10000")); // thoi gian giua cac lan
                                                                                              // su kien xay ra
            luckyEventDurationTime = Integer.parseInt(prop.getProperty("lucky.event.duration.time", "3000")); // thoi
                                                                                                              // gian
                                                                                                              // keo dai
                                                                                                              // su kien

            enableLuckyNumbers = Boolean.parseBoolean(prop.getProperty("lucky.numbers.enable", "true"));
            luckyNumberMultiplier = Integer.parseInt(prop.getProperty("lucky.numbers.multiplier", "3"));

            skillLightPrice = Integer.parseInt(prop.getProperty("skill.light.price", "100"));
            skillDarkPrice = Integer.parseInt(prop.getProperty("skill.dark.price", "150"));
            skillFreezePrice = Integer.parseInt(prop.getProperty("skill.freeze.price", "200"));

            videoRewardEnabled = Boolean.parseBoolean(prop.getProperty("video.reward.enabled", "true"));
            videoRewardAmount = Integer.parseInt(prop.getProperty("video.reward.amount", "1000"));
            videoDailyLimit = Integer.parseInt(prop.getProperty("video.reward.daily.limit", "5"));
            videoUrl = prop.getProperty("video.reward.url", "/images/302281_tiny.mp4");

            // System.out.println("=== SERVER CONFIGURATION ===");
            // System.out.println("Board Size: " + boardSize);
            // System.out.println("Game Time: " + gameTime + " seconds");
            // System.out.println("Max Players: " + maxPlayers);
            // System.out.println("Win Gold Reward: " + winGoldReward);
            // System.out.println("Lucky Number Bonus: " + luckyNumberBonus);
            // System.out.println("Lucky Numbers Enabled: " + enableLuckyNumbers);
            // System.out.println("Lucky Numbers Count: " + luckyNumberCount);
            // System.out.println("Lucky Number Multiplier: x" + luckyNumberMultiplier);
            // System.out.println("============================");

        } catch (Exception e) {
            System.out.println("Lỗi đọc config, dùng mặc định: " + e.getMessage());
            setDefaultValues();
        }
    }

    private void createDefaultConfig() {
        Properties prop = new Properties();

        prop.setProperty("board.size", "100");
        prop.setProperty("game.time", "120");
        prop.setProperty("max.players", "2");

        prop.setProperty("win.gold.reward", "100");
        prop.setProperty("lucky.number.bonus", "50");
        prop.setProperty("lucky.event.time", "10000");
        prop.setProperty("lucky.event.duration.time", "3000");

        prop.setProperty("lucky.numbers.enable", "true");
        prop.setProperty("lucky.numbers.count", "10");
        prop.setProperty("lucky.numbers.multiplier", "3");

        prop.setProperty("skill.light.price", "100");
        prop.setProperty("skill.dark.price", "150");
        prop.setProperty("skill.freeze.price", "200");

        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            prop.store(fos, "Server Configuration File");
            System.out.println("Created default config.properties");
        } catch (IOException e) {
            System.err.println("Failed to create default config: " + e.getMessage());
        }
    }

    private void setDefaultValues() {
        boardSize = 100;
        gameTime = 120;
        maxPlayers = 2;
        winGoldReward = 100;
        luckyNumberBonus = 50;
        enableLuckyNumbers = true;
        luckyEventTime = 10000;
        luckyEventDurationTime = 3000;
        luckyNumberMultiplier = 3;
        skillLightPrice = 100;
        skillDarkPrice = 150;
        skillFreezePrice = 200;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getGameTime() {
        return gameTime;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getWinGoldReward() {
        return winGoldReward;
    }

    public int getLuckyNumberBonus() {
        return luckyNumberBonus;
    }

    public boolean isEnableLuckyNumbers() {
        return enableLuckyNumbers;
    }

    public int getLuckyEventTime() {
        return luckyEventTime;
    }

    public int getLuckyEventDurationTime() {
        return luckyEventDurationTime;
    }

    public int getLuckyNumberMultiplier() {
        return luckyNumberMultiplier;
    }

    public int getSkillLightPrice() {
        return skillLightPrice;
    }

    public int getSkillDarkPrice() {
        return skillDarkPrice;
    }

    public int getSkillFreezePrice() {
        return skillFreezePrice;
    }

    public boolean isVideoRewardEnabled() {
        return videoRewardEnabled;
    }

    public int getVideoRewardAmount() {
        return videoRewardAmount;
    }

    public int getVideoDailyLimit() {
        return videoDailyLimit;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}