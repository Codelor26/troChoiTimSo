package com.timso.server.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.timso.server.config.ServerConfig;
import com.timso.server.dao.UserDAO;

public class Room implements Runnable {

    private ClientHandler player1;
    private ClientHandler player2;
    private int currentTarget;
    private Map<String, Integer> scores = new HashMap<>();
    private long endTime;

    private boolean waitingForRematch = false;
    private ClientHandler rematchRequester = null;

    private int luckyNumber;
    private long luckyEventEndTime;
    private boolean isLuckyEventActive = false;
    private int normalTarget;

    private List<Integer> numberList;
    private List<Integer> luckyNumbersUsed = new ArrayList<>();
    private List<Integer> targetNumbersUsed = new ArrayList<>();

    private static ServerConfig config = new ServerConfig();

    public Room(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;

        scores.put(getPlayerName(p1), 0);
        scores.put(getPlayerName(p2), 0);

        p1.setRoom(this);
        p2.setRoom(this);

        generateNumberList();

        System.out.println("Room created for: " + getPlayerName(p1) + " vs " + getPlayerName(p2));
    }

    @Override
    public void run() {
        startGame();
    }

    private void startLuckyEvent() {
        if (isLuckyEventActive)
            return;

        Random rand = new Random();

        List<Integer> availableNumbers = new ArrayList<>();
        for (int i = 1; i <= config.getBoardSize(); i++) {
            if (!luckyNumbersUsed.contains(i) && !targetNumbersUsed.contains(i)) {
                availableNumbers.add(i);
            }
        }

        if (availableNumbers.isEmpty()) {
            System.out.println("No more lucky numbers available!");
            return;
        }

        luckyNumber = availableNumbers.get(rand.nextInt(availableNumbers.size()));
        luckyNumbersUsed.add(luckyNumber);

        isLuckyEventActive = true;
        luckyEventEndTime = config.getLuckyEventDurationTime(); // Thời gian kéo dài của sự kiện

        String msg = "LUCKY_EVENT|" + luckyNumber + "|3";
        sendToBoth(msg);
        System.out.println("🎲 LUCKY EVENT! Find number " + luckyNumber + " in 3 seconds for bonus points!");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                isLuckyEventActive = false;
                sendToBoth("LUCKY_EVENT_END");
                System.out.println("Lucky event ended");
            } catch (InterruptedException e) {
            }
        }).start();
    }

    private void generateNumberList() {
        numberList = new ArrayList<>();
        for (int i = 1; i <= config.getBoardSize(); i++) {
            numberList.add(i);
        }
        Collections.shuffle(numberList);
        luckyNumbersUsed = new ArrayList<>();
        targetNumbersUsed = new ArrayList<>();
        System.out.println("Number list generated with " + numberList.size() + " numbers");

    }

    private void generateLuckyNumber() {
        if (!config.isEnableLuckyNumbers()) {
            luckyNumber = -1;
            return;
        }
        Random rand = new Random();
        luckyNumber = rand.nextInt(config.getBoardSize()) + 1;
        System.out.println("🎲 Lucky number for this game: " + luckyNumber + " (x" + config.getLuckyNumberMultiplier()
                + " points!)");
    }

    private void sendLuckyNumberAnnouncement() {
        if (luckyNumber != -1) {
            sendToBoth("LUCKY_ANNOUNCEMENT|Số may mắn của ván này là: " + luckyNumber + "! (x"
                    + config.getLuckyNumberMultiplier() + " điểm)");
        }
    }

    private String getPlayerName(ClientHandler player) {
        String name = player.getPlayername();
        return (name != null && !name.isEmpty()) ? name : player.getUsername();
    }

    private String encodeNumberList(List<Integer> numbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(numbers.get(i));
        }
        return encode(sb.toString());
    }

    private String encode(String value) {
        return java.util.Base64.getEncoder().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public void startGame() {
        System.out.println("Starting game between " + getPlayerName(player1) + " and " + getPlayerName(player2));
        int gameDuration = config.getGameTime() * 1000;

        if (!numberList.isEmpty()) {
            currentTarget = numberList.get(0);
        } else {
            currentTarget = 1;
        }

        endTime = System.currentTimeMillis() + gameDuration;

        String numbersStr = encodeNumberList(numberList);
        sendToBoth("BOARD_NUMBERS|" + numbersStr);
        System.out.println("Sent BOARD_NUMBERS: " + numberList.size() + " numbers");

        sendToBoth("START_GAME|" + currentTarget + "|" + config.getGameTime());

        generateLuckyNumber();
        sendLuckyNumberAnnouncement();

        System.out.println("Game started with target: " + currentTarget);

        new Thread(() -> {
            while (System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(1000);
                    if ((player1 != null && player1.isDisconnected()) ||
                            (player2 != null && player2.isDisconnected())) {
                        checkPlayerDisconnected();
                        return;
                    }
                    int remainingSeconds = (int) ((endTime - System.currentTimeMillis()) / 1000);
                    sendToBoth("TIME_UPDATE|" + remainingSeconds);
                } catch (InterruptedException e) {
                    break;
                }
            }
            endGame();
        }).start();

        new Thread(() -> {
            long lastEventTime = System.currentTimeMillis();
            int eventInterval = config.getLuckyEventTime(); // Thời gian giữa các lần kích hoạt sự kiện

            while (System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(1000);

                    // Mỗi 20 giây, random kích hoạt sự kiện
                    if (!isLuckyEventActive && System.currentTimeMillis() - lastEventTime > eventInterval) {
                        if (new Random().nextInt(100) < 30) {
                            startLuckyEvent();
                            lastEventTime = System.currentTimeMillis();
                        }
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public synchronized void handlePick(ClientHandler player, int number) {
        ClientHandler opponent = (player == player1) ? player2 : player1;
        if (opponent == null || opponent.isDisconnected()) {
            String winnerName = getPlayerName(player);
            int currentScore = scores.getOrDefault(winnerName, 0) + 1;
            scores.put(winnerName, currentScore);

            String result = "GAME_OVER|" + encode(winnerName) + "|" + currentScore + "|0";
            player.sendMessage(result);
            player.sendMessage("GAME_END|Đối thủ đã thoát game, bạn thắng!");

            player.setRoom(null);
            if (opponent != null)
                opponent.setRoom(null);
            return;
        }

        if (System.currentTimeMillis() > endTime) {
            player.sendMessage("GAME_OVER|Time's up!");
            return;
        }

        if (isLuckyEventActive) {
            if (number == luckyNumber) {
                String playerName = getPlayerName(player);
                int bonusPoints = config.getLuckyNumberMultiplier(); // x3 điểm
                int newScore = scores.getOrDefault(playerName, 0) + bonusPoints;
                scores.put(playerName, newScore);

                sendToBoth("CORRECT|" + encode(playerName) + "|" + number + "|" + newScore);
                sendToBoth("LUCKY_BONUS|" + encode(playerName) + "|" + luckyNumber + "|+" + bonusPoints);
                System.out.println("🎉 " + playerName + " got LUCKY BONUS! +" + bonusPoints + " points!");

                isLuckyEventActive = false;
                sendToBoth("LUCKY_EVENT_END");

                if (number == currentTarget) {
                    numberList.remove(Integer.valueOf(number));
                    targetNumbersUsed.add(number);

                    if (!numberList.isEmpty()) {
                        currentTarget = numberList.get(0);
                        sendToBoth("NEW_TARGET|" + currentTarget);
                        System.out.println("Lucky number was also target! New target: " + currentTarget);
                    } else {
                        endGame();
                    }
                }

                return;
            } else {
                player.sendMessage("WRONG|" + number);
                return;
            }
        }

        if (number != currentTarget) {
            player.sendMessage("WRONG|" + number);
            return;
        }

        String playerName = getPlayerName(player);
        int points = 1;
        int newScore = scores.getOrDefault(playerName, 0) + points;
        scores.put(playerName, newScore);
        System.out.println("Correct! " + playerName + " picked " + number + " (+1 point)");

        numberList.remove(Integer.valueOf(number));

        sendToBoth("CORRECT|" + encode(playerName) + "|" + number + "|" + newScore);

        if (!numberList.isEmpty()) {
            currentTarget = numberList.get(0);
            sendToBoth("NEW_TARGET|" + currentTarget);
            System.out.println("New target: " + currentTarget + ", remaining: " + numberList.size());
        } else {
            endGame();
        }

    }

    private void endGame() {
        String p1Name = getPlayerName(player1);
        String p2Name = getPlayerName(player2);

        int p1Score = scores.getOrDefault(p1Name, 0);
        int p2Score = scores.getOrDefault(p2Name, 0);

        String winner;
        int winnerGold = config.getWinGoldReward();

        if (p1Score > p2Score) {
            winner = p1Name;
            addGoldToPlayer(player1, winnerGold);

        } else if (p2Score > p1Score) {
            winner = p2Name;
            addGoldToPlayer(player2, winnerGold);

        } else {
            winner = "DRAW";
        }

        String resultForP1 = "GAME_OVER|" + encode(winner) + "|" + p1Score + "|" + p2Score;
        player1.sendMessage(resultForP1);

        String resultForP2 = "GAME_OVER|" + encode(winner) + "|" + p2Score + "|" + p1Score;
        player2.sendMessage(resultForP2);

        if (!"DRAW".equals(winner)) {
            UserDAO userDao = new UserDAO();
            int newGold = userDao.getUserGold(winner.equals(p1Name) ? player1.getUsername() : player2.getUsername());

            String goldMessage = "GOLD_UPDATE|" + winnerGold + "|" + newGold;
            if (winner.equals(p1Name)) {
                player1.sendMessage(goldMessage);
            } else {
                player2.sendMessage(goldMessage);
            }
        }

        System.out.println("Game ended - " + p1Name + ": " + p1Score + " vs " + p2Name + ": " + p2Score);
        System.out.println("Winner: " + winner + " (+" + winnerGold + " gold)");
    }

    private void addGoldToPlayer(ClientHandler player, int goldAmount) {
        if (player == null)
            return;

        String username = player.getUsername();
        UserDAO userDao = new UserDAO();
        boolean success = userDao.addGold(username, goldAmount);

        if (success) {
            System.out.println("Added " + goldAmount + " gold to " + player.getPlayername());
        } else {
            System.out.println("Failed to add gold to " + player.getPlayername());
        }
    }

    public synchronized void checkPlayerDisconnected() {
        if (player1 == null && player2 == null)
            return;

        boolean p1Disconnected = (player1 == null || player1.isDisconnected());
        boolean p2Disconnected = (player2 == null || player2.isDisconnected());

        if (p1Disconnected && !p2Disconnected && player2 != null) {
            String winnerName = getPlayerName(player2);
            int winnerScore = scores.getOrDefault(winnerName, 0);
            int winnerGold = config.getWinGoldReward();

            addGoldToPlayer(player2, winnerGold);

            UserDAO userDao = new UserDAO();
            int newGold = userDao.getUserGold(player2.getUsername());

            String result = "GAME_OVER|" + encode(winnerName) + "|" + winnerScore + "|0";
            player2.sendMessage(result);
            player2.sendMessage("GAME_END|Đối thủ đã thoát game, bạn thắng! +" + winnerGold + " vàng!");
            player2.sendMessage("GOLD_UPDATE|" + winnerGold + "|" + newGold);

        } else if (!p1Disconnected && p2Disconnected && player1 != null) {
            String winnerName = getPlayerName(player1);
            int winnerScore = scores.getOrDefault(winnerName, 0);
            int winnerGold = config.getWinGoldReward();

            addGoldToPlayer(player1, winnerGold);

            UserDAO userDao = new UserDAO();
            int newGold = userDao.getUserGold(player1.getUsername());

            String result = "GAME_OVER|" + encode(winnerName) + "|" + winnerScore + "|0";
            player1.sendMessage(result);
            player1.sendMessage("GAME_END|Đối thủ đã thoát game, bạn thắng! +" + winnerGold + " vàng!");
            player1.sendMessage("GOLD_UPDATE|" + winnerGold + "|" + newGold);
        }
    }

    public synchronized void requestRematch(ClientHandler player) {
        if (waitingForRematch) {
            if (rematchRequester == player) {
                player.sendMessage("REMATCH_ERROR|Bạn đã yêu cầu rồi");
                return;
            }

            sendToBoth("REMATCH_ACCEPTED");

            Room newRoom = new Room(player1, player2);
            new Thread(newRoom).start();

            waitingForRematch = false;
            rematchRequester = null;

        } else {
            waitingForRematch = true;
            rematchRequester = player;

            String requesterName = getPlayerName(player);

            ClientHandler opponent = (player == player1) ? player2 : player1;
            if (opponent != null) {
                opponent.sendMessage("REMATCH_REQUEST|" + encode(requesterName));
            }

            player.sendMessage("REMATCH_WAITING|Đang chờ đối thủ đồng ý...");

            new Thread(() -> {
                try {
                    Thread.sleep(30000);
                    if (waitingForRematch && rematchRequester != null) {
                        waitingForRematch = false;
                        if (rematchRequester != null) {
                            rematchRequester.sendMessage("REMATCH_TIMEOUT|Hết thời gian chờ");
                        }
                        rematchRequester = null;
                    }
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }

    public synchronized void rejectRematch(ClientHandler player) {
        if (!waitingForRematch)
            return;

        String rejecterName = getPlayerName(player);
        System.out.println(rejecterName + " rejected rematch");

        if (rematchRequester != null) {
            rematchRequester.sendMessage("REMATCH_REJECTED|" + encode(rejecterName));
        }

        sendToBoth("REMATCH_CANCELLED|" + encode(rejecterName) + " đã từ chối");

        waitingForRematch = false;
        rematchRequester = null;
    }

    public void cancelRematch() {
        waitingForRematch = false;
        rematchRequester = null;
        sendToBoth("REMATCH_CANCELLED");
    }

    private void sendToBoth(String msg) {
        if (player1 != null)
            player1.sendMessage(msg);
        if (player2 != null)
            player2.sendMessage(msg);
    }

    public synchronized void useFreezeSkill(ClientHandler user) {
        ClientHandler opponent = (user == player1) ? player2 : player1;

        if (opponent == null || opponent.isDisconnected()) {
            user.sendMessage("SKILL_FAIL|Đối thủ không còn trong phòng");
            return;
        }

        opponent.sendMessage("FREEZE_PLAYER|3");
        user.sendMessage("SKILL_SUCCESS|Đã đóng băng đối thủ trong 3 giây");

        System.out.println(getPlayerName(user) + " used FREEZE SKILL on " + getPlayerName(opponent));

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                opponent.sendMessage("UNFREEZE_PLAYER");
            } catch (InterruptedException e) {
            }
        }).start();
    }

    public synchronized void useDarkSkill(ClientHandler user) {
        ClientHandler opponent = (user == player1) ? player2 : player1;

        if (opponent == null || opponent.isDisconnected()) {
            user.sendMessage("SKILL_FAIL|Đối thủ không còn trong phòng");
            return;
        }

        opponent.sendMessage("BLOCK_NUMBERS|3");
        user.sendMessage("SKILL_SUCCESS|Đã che số đối thủ trong 3 giây");

        System.out.println(getPlayerName(user) + " used DARK SKILL on " + getPlayerName(opponent));

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                opponent.sendMessage("UNBLOCK_NUMBERS");
            } catch (InterruptedException e) {
            }
        }).start();
    }

    public Map<String, Integer> getScores() {
        return new HashMap<>(scores);
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }
}