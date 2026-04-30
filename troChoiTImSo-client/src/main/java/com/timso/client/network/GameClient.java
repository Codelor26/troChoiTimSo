package com.timso.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.timso.client.controller.*;

public class GameClient {
    private static GameClient instance;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connected = false;
    private String username;
    private GameListener listener;

    private List<String> pendingMessages = new ArrayList<>();

    private GameClient() {
    }

    public static GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }
        return instance;
    }

    public void connect(String username) {
        try {
            this.username = username;
            socket = new Socket("localhost", 12345);
            writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            connected = true;

            writer.println("PLAYER_INFO" + "|" + encode(username));

            new Thread(this::listenForMessages).start();

            System.out.println("GameClient connected for: " + username);
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                if (line.equals("<END>"))
                    continue;
                // System.out.println("GameClient received: " + line);

                processMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connected = false;
        }
    }

    private void processMessage(String message) {
        if (listener == null) {
            synchronized (pendingMessages) {
                System.out.println("Listener not set yet, queueing message: " + message);
                pendingMessages.add(message);
            }
            return;
        }

        String[] parts = message.split("\\|");
        String command = parts[0];

        switch (command) {
            case "WAITING":
                listener.onWaiting();
                break;
            case "MATCH_FOUND":
                if (parts.length >= 2) {
                    String opponentName = decode(parts[1]);
                    String opponentAvatar = (parts.length >= 3) ? decode(parts[2]) : "";
                    System.out.println("MATCH_FOUND - opponent: " + opponentName + ", avatar: " + opponentAvatar);
                    listener.onMatchFound(opponentName, opponentAvatar);
                } else {
                    System.err.println("MATCH_FOUND - invalid parts: " + parts.length);
                }
                break;
            case "START_GAME":
                if (parts.length >= 3) {
                    int target = Integer.parseInt(parts[1]);
                    int duration = Integer.parseInt(parts[2]);
                    listener.onGameStart(target, duration);
                }
                break;
            case "CORRECT":
                if (parts.length >= 4) {
                    String playerName = decode(parts[1]);
                    int number = Integer.parseInt(parts[2]);
                    int score = Integer.parseInt(parts[3]);
                    listener.onCorrect(playerName, number, score);
                }
                break;
            case "WRONG":
                if (parts.length >= 2) {
                    int wrongNumber = Integer.parseInt(parts[1]);
                    listener.onWrong(wrongNumber);
                }
                break;
            case "NEW_TARGET":
                if (parts.length >= 2) {
                    listener.onNewTarget(Integer.parseInt(parts[1]));
                }
                break;
            case "GAME_OVER":
                if (parts.length >= 4) {
                    String winner = decode(parts[1]);
                    int yourScore = Integer.parseInt(parts[2]);
                    int opponentScore = Integer.parseInt(parts[3]);
                    listener.onGameOver(winner, yourScore, opponentScore);
                }
                break;
            case "BOARD_NUMBERS":
                System.out.println("=== BOARD_NUMBERS received, listener="
                        + (listener != null ? listener.getClass().getSimpleName() : "null"));
                if (parts.length >= 2) {
                    String numbersStr = decode(parts[1]);
                    if (listener != null) {
                        listener.onBoardNumbers(numbersStr);
                    } else {
                        System.out.println("Listener is null, cannot deliver BOARD_NUMBERS");
                    }
                }
                break;
            case "TIME_UPDATE":
                if (parts.length >= 2) {
                    int remainingSeconds = Integer.parseInt(parts[1]);
                    listener.onTimeUpdate(remainingSeconds);
                }
                break;
            case "REMATCH_REQUEST":
                if (parts.length >= 2) {
                    String requester = decode(parts[1]);
                    System.out.println("=== PROCESSING REMATCH_REQUEST from: " + requester + " ===");
                    if (listener != null) {
                        listener.onRematchRequest(requester);
                    } else {
                        System.out.println("WARNING: listener is null, cannot deliver REMATCH_REQUEST");
                    }
                }
                break;
            case "REMATCH_ACCEPTED":
                listener.onRematchAccepted();
                break;
            case "REMATCH_TIMEOUT":
                listener.onRematchTimeout();
                break;
            case "REMATCH_ERROR":
                listener.onRematchError(parts[1]);
                break;
            case "REMATCH_REJECTED":
                if (parts.length >= 2) {
                    String rejecter = decode(parts[1]);
                    listener.onRematchRejected(rejecter);
                }
                break;
            case "REMATCH_CANCELLED":
                listener.onRematchCancelled();
                break;
            case "REMATCH_WAITING":
                listener.onRematchWaiting(parts[1]);
                break;
            case "GAME_END":
                if (parts.length >= 2) {
                    String message1 = parts[1];
                    listener.onGameEnd(message1);
                }
                break;
            case "GOLD_UPDATE":
                if (parts.length >= 3) {
                    int goldAdded = Integer.parseInt(parts[1]);
                    int newGold = Integer.parseInt(parts[2]);
                    listener.onGoldUpdate(goldAdded, newGold);
                }
                break;
            case "LUCKY_EVENT":
                int luckyNum = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);
                listener.onLuckyEvent(luckyNum, duration);
                break;

            case "LUCKY_EVENT_END":
                listener.onLuckyEventEnd();
                break;

            case "LUCKY_BONUS":
                String playerName = decode(parts[1]);
                int number = Integer.parseInt(parts[2]);
                String bonus = parts[3];
                listener.onLuckyBonus(playerName, number, bonus);
                break;

            case "FREEZE_PLAYER":
                int freezeDuration = Integer.parseInt(parts[1]);
                listener.onFreezePlayer(freezeDuration);
                break;

            case "UNFREEZE_PLAYER":
                listener.onUnfreezePlayer();
                break;

            case "BLOCK_NUMBERS":
                int blockDuration = Integer.parseInt(parts[1]);
                listener.onBlockNumbers(blockDuration);
                break;

            case "UNBLOCK_NUMBERS":
                listener.onUnblockNumbers();
                break;

            case "VIDEO_REWARD_SUCCESS":
                if (parts.length >= 3) {
                    int rewardAmount = Integer.parseInt(parts[1]);
                    int newGold = Integer.parseInt(parts[2]);
                    listener.onVideoRewardSuccess(rewardAmount, newGold);
                }
                break;

            case "VIDEO_REWARD_FAIL":
                listener.onVideoRewardFail(parts[1]);
                break;
        }
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
        System.out.println("Listener set for GameClient");

        synchronized (pendingMessages) {
            System.out.println("Processing " + pendingMessages.size() + " pending messages");
            for (String message : pendingMessages) {
                System.out.println("Processing queued message: " + message);
                processMessage(message);
            }
            pendingMessages.clear();
        }
    }

    public void findMatch() {
        if (writer != null) {
            writer.println("FIND_MATCH");
            System.out.println("Sent FIND_MATCH request");
        }
    }

    public void cancelMatch() {
        if (writer != null) {
            writer.println("CANCEL_MATCH");
        }
    }

    public void pickNumber(int number) {
        if (writer != null) {
            writer.println("PICK" + "|" + number);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        try {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    public void sendToServer(String message) {
        if (writer != null && connected) {
            writer.println(message);
            writer.flush();
            System.out.println("Sent to server: " + message);
        } else {
            System.err.println("Cannot send to server - not connected");
        }
    }

    public void requestRematch() {
        if (writer != null) {
            writer.println("REQUEST_REMATCH");
            writer.flush();
            System.out.println("Sent REQUEST_REMATCH to server");
        }
    }

    public void cancelRematch() {
        if (writer != null) {
            writer.println("CANCEL_REMATCH");
            writer.flush();
            System.out.println("Sent CANCEL_REMATCH to server");
        }
    }

    public void leaveGame() {
        sendToServer("PLAYER_LEAVE_GAME");
    }

    public interface GameListener {
        void onWaiting();

        void onMatchFound(String opponentName, String opponentAvatar);

        void onBoardNumbers(String numbersStr);

        void onGameStart(int currentTarget, int duration);

        void onTimeUpdate(int remainingSeconds);

        void onCorrect(String playerName, int number, int score);

        void onWrong(int number);

        void onNewTarget(int target);

        void onGameOver(String winner, int yourScore, int opponentScore);

        void onRematchRequest(String requester);

        void onRematchAccepted();

        void onRematchTimeout();

        void onRematchError(String error);

        void onRematchRejected(String rejecter);

        void onRematchCancelled();

        void onRematchWaiting(String message);

        void onGameEnd(String message);

        void onGoldUpdate(int goldAdded, int newGold);

        void onLuckyEvent(int luckyNumber, int duration);

        void onLuckyEventEnd();

        void onLuckyBonus(String playerName, int number, String bonus);

        void onFreezePlayer(int duration);

        void onUnfreezePlayer();

        void onBlockNumbers(int duration);

        void onUnblockNumbers();

        void onVideoRewardSuccess(int rewardAmount, int newGold);

        void onVideoRewardFail(String message);
    }

}