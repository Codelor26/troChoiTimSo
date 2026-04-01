package com.timso.server.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Room {
    private List<ClientHandler> players;
    private int currentTarget;

    private List<Integer> numberList;
    private Map<String, Integer> scores;
    private Set<Integer> luckyNumbers;

    private long endTime;

    public Room(List<ClientHandler> players) {
        this.players = players;
        this.scores = new HashMap<>();
        this.luckyNumbers = new HashSet<>();

        for (ClientHandler p : players) {
            p.setRoom(this);
            scores.put(p.getUsername(), 0);
        }

        generateNumbers();
        generateLuckyNumbers();
    }

    private void generateNumbers() {
        numberList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numberList.add(i);
        }
        Collections.shuffle(numberList);
    }

    private void generateLuckyNumbers() {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            luckyNumbers.add(rand.nextInt(100) + 1);
        }
    }

    public void startGame() {
        broadcast("START_GAME");

        endTime = System.currentTimeMillis() + 120000;

        new Thread(() -> {
            while (System.currentTimeMillis() < endTime) {
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
            endGame();
        }).start();

        nextTarget();
    }

    private void nextTarget() {
        if (numberList.isEmpty()) {
            endGame();
            return;
        }

        currentTarget = numberList.remove(0);
        broadcast("TARGET:" + currentTarget);
    }

    public void broadcast(String msg) {
        for (ClientHandler p : players) {
            p.sendMessage(msg);
        }
    }

    // gửi cho tất cả trừ 1 người người dùng skill
    private void broadcastExcept(ClientHandler except, String msg) {
        for (ClientHandler p : players) {
            if (p != except) {
                p.sendMessage(msg);
            }
        }
    }

    // xử lý click
    public synchronized void handleClick(ClientHandler player, int number) {

        if (System.currentTimeMillis() > endTime) return;

        if (number == currentTarget) {
            String user = player.getUsername();
            int score = scores.get(user);

            if (luckyNumbers.contains(number)) {
                score += 5;
            } else {
                score += 1;
            }

            scores.put(user, score);

            broadcast("CORRECT:" + user + ":" + number);

            nextTarget();
        }
    }
        // skill che màn hình
    public void useHideSkill(ClientHandler player) {
        // gửi cho người khác
        broadcastExcept(player, "HIDE:" + player.getUsername());

        // sau 3 giây hiện lại
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}

            broadcastExcept(player, "SHOW");
        }).start();
    }

    private void endGame() {
        String winner = Collections.max(scores.entrySet(),
                Map.Entry.comparingByValue()).getKey();

        broadcast("END:" + winner);
    }
}
