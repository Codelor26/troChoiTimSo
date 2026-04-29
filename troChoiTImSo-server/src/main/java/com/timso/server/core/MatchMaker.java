package com.timso.server.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MatchMaker {

    private static final List<ClientHandler> waitingPlayers = new ArrayList<>();

    public static synchronized void addPlayer(ClientHandler player) {
        if (player == null) {
            System.out.println("Cannot add null player to matchmaker");
            return;
        }

        System.out.println("Adding player to queue: " + player.getUsername());
        waitingPlayers.add(player);
        player.sendMessage("WAITING");
        tryMatch();
    }

    public static synchronized void removePlayer(ClientHandler player) {
        if (player == null)
            return;

        Room room = player.getRoom();
        if (room != null) {
            room.checkPlayerDisconnected();
        }
        boolean removed = waitingPlayers.remove(player);
        if (removed) {
            System.out.println("Removed player from queue: " + player.getUsername());
        }
    }

    private static void tryMatch() {
        while (waitingPlayers.size() >= 2) {
            ClientHandler p1 = waitingPlayers.remove(0);
            ClientHandler p2 = waitingPlayers.remove(0);

            if (!isPlayerValid(p1) || !isPlayerValid(p2)) {
                if (isPlayerValid(p1))
                    waitingPlayers.add(p1);
                if (isPlayerValid(p2))
                    waitingPlayers.add(p2);
                continue;
            }

            String p1Name = p1.getPlayername();
            String p1Avatar = p1.getPlayerAvatar();
            String p2Name = p2.getPlayername();
            String p2Avatar = p2.getPlayerAvatar();

            System.out.println("p1.getUsername(): " + p1.getUsername());
            System.out.println("p1.getPlayername(): " + p1.getPlayername());
            System.out.println("p1Name variable: " + p1Name);
            System.out.println("p2.getUsername(): " + p2.getUsername());
            System.out.println("p2.getPlayername(): " + p2.getPlayername());
            System.out.println("p2Name variable: " + p2Name);

            Room room = new Room(p1, p2);
            p1.setRoom(room);
            p2.setRoom(room);

            String msgToP1 = "MATCH_FOUND|" + encode(p2Name) + "|" + encode(p2Avatar != null ? p2Avatar : "");
            String msgToP2 = "MATCH_FOUND|" + encode(p1Name) + "|" + encode(p1Avatar != null ? p1Avatar : "");

            System.out.println("Message to p1: " + msgToP1);
            System.out.println("Message to p2: " + msgToP2);

            p1.sendMessage(msgToP1);
            p2.sendMessage(msgToP2);

            new Thread(room).start();
        }
    }

    private static String getSafePlayerAvatar(ClientHandler player) {
        String avatar = player.getPlayerAvatar();
        return (avatar != null && !avatar.isEmpty()) ? avatar : "";
    }

    private static boolean isPlayerValid(ClientHandler player) {
        return player != null && !player.isDisconnected();
    }

    private static String getSafePlayerName(ClientHandler player) {
        String name = player.getPlayername();
        System.out.println("getSafePlayerName for " + player.getUsername() + " = '" + name + "'");
        return name;
    }

    private static String encode(String value) {
        if (value == null) {
            value = "";
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}