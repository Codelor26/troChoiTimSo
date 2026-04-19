package com.timso.server.config;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConfig {
    private int boardSize;
    private int gameTime;
    private int maxPlayers;

    public ServerConfig() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("config.properties"));

            boardSize = Integer.parseInt(prop.getProperty("board.size", "100"));
            gameTime = Integer.parseInt(prop.getProperty("game.time", "120"));
            maxPlayers = Integer.parseInt(prop.getProperty("max.players", "3"));

        } catch (Exception e) {
            System.out.println("Lỗi đọc config, dùng mặc định");
            boardSize = 100; // số lượng sô cần tìm
            gameTime = 120; // thời gian 1 ván
            maxPlayers = 2; //số người chơi
        }
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
}