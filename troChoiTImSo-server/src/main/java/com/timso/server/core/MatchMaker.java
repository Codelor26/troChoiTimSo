package com.timso.server.core;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchMaker {

    private static final ConcurrentLinkedQueue<ClientHandler> waitingQueue = new ConcurrentLinkedQueue<>();

    public static synchronized void findMatch(ClientHandler player) {
        System.out.println("Player vào queue: " + player.getName());

        if (!waitingQueue.isEmpty()) {
            ClientHandler opponent = waitingQueue.poll();

            if (opponent != null) {
                System.out.println("Ghép trận thành công!");

                Room room = new Room(opponent, player);
                room.startGame();
                return;
            }
        }

        waitingQueue.add(player);
        player.send("WAITING");
    }
}
