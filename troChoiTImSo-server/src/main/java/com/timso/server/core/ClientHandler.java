package com.timso.server.core;

import com.timso.common.model.User;
import com.timso.server.dao.StatsDAO;
import com.timso.server.dao.UserDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Room room;
    private String username;

    private String playerName;
    private String playerAvatar;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);

            String messageFromClient;
            while (running.get() && (messageFromClient = reader.readLine()) != null) {
                messageFromClient = messageFromClient.trim();
                if (messageFromClient.isEmpty()) {
                    continue;
                }

                if ("bye".equalsIgnoreCase(messageFromClient)) {
                    sendSuccess("GOODBYE");
                    break;
                }

                processCommand(messageFromClient);
            }
        } catch (SocketException e) {
            if (running.get()) {
                System.out.println("SocketException for " + clientInfo + ": " + e.getMessage());
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("IOException for " + clientInfo + ": " + e.getMessage());
            }
        } finally {
            closeResources();
            System.out.println("Client handler stopped for " + clientInfo + ".");
        }
    }

    private void processCommand(String request) {
        String[] tokens = request.split("\\|", -1);
        if (tokens.length == 0) {
            sendError("Yêu cầu không hợp lệ.");
            return;
        }

        String command = tokens[0].trim().toUpperCase(Locale.ROOT);
        switch (command) {
            case "LOGIN" -> handleLogin(tokens);
            case "REGISTER" -> handleRegister(tokens);
            case "RESET_PASSWORD" -> handleResetPassword(tokens);
            case "UPDATE_PROFILE" -> handleUpdateProfile(tokens);
            case "UPDATE_USER_INFO" -> handleUpdateUserInfo(tokens);

            case "PLAYER_INFO" -> handlePlayerInfo(tokens);
            case "FIND_MATCH" -> handleFindMatch();
            case "CANCEL_MATCH" -> handleCancelMatch();
            case "PICK" -> handlePick(tokens);

            case "REQUEST_REMATCH" -> {
                if (room != null) {
                    room.requestRematch(this);
                }
            }
            case "CANCEL_REMATCH" -> {
                if (room != null) {
                    room.cancelRematch();
                }
            }
            case "REJECT_REMATCH" -> {
                if (room != null) {
                    room.rejectRematch(this);
                }
            }

            case "PLAYER_LEAVE_GAME" -> {
                if (room != null) {
                    ClientHandler otherPlayer = (this == room.getPlayer1()) ? room.getPlayer2() : room.getPlayer1();

                    if (otherPlayer != null && !otherPlayer.isDisconnected()) {
                        String winnerName = otherPlayer.getPlayername();
                        int winnerScore = room.getScores().getOrDefault(winnerName, 0);
                        int winnerGold = 100;

                        UserDAO userDao = new UserDAO();
                        boolean success = userDao.addGold(otherPlayer.getUsername(), winnerGold);

                        if (success) {
                            int newGold = userDao.getUserGold(otherPlayer.getUsername());
                            String result = "GAME_OVER|" + encode(winnerName) + "|" + winnerScore + "|0";
                            otherPlayer.sendMessage(result);
                            otherPlayer
                                    .sendMessage("GAME_END|Đối thủ đã rời game, bạn thắng! +" + winnerGold + " vàng!");
                            otherPlayer.sendMessage("GOLD_UPDATE|" + winnerGold + "|" + newGold);

                            System.out.println("Player " + getPlayername() + " left. " + winnerName + " wins! +"
                                    + winnerGold + " gold");
                        } else {
                            String result = "GAME_OVER|" + encode(winnerName) + "|" + winnerScore + "|0";
                            otherPlayer.sendMessage(result);
                            otherPlayer.sendMessage("GAME_END|Đối thủ đã rời game, bạn thắng!");
                        }

                    }

                    room = null;
                }
                sendMessage("LEAVE_OK");
            }

            case "BUY_SKILL" -> {
                if (tokens.length >= 3) {
                    String skillType = tokens[1];
                    int price = Integer.parseInt(tokens[2]);

                    UserDAO userDao = new UserDAO();

                    int currentGold = userDao.getUserGold(username);
                    if (currentGold >= price) {
                        boolean goldSuccess = userDao.deductGold(username, price);
                        if (goldSuccess) {
                            boolean skillSuccess = userDao.updateSkill(username, skillType, 1);
                            if (skillSuccess) {
                                Map<String, Integer> skills = userDao.getUserSkills(username);
                                int newGold = userDao.getUserGold(username);

                                sendMessage("BUY_SKILL_SUCCESS|" + skillType + "|" +
                                        skills.get(skillType) + "|" + newGold);
                            } else {
                                userDao.addGold(username, price);
                                sendMessage("BUY_SKILL_FAIL|Lỗi lưu kỹ năng");
                            }
                        } else {
                            sendMessage("BUY_SKILL_FAIL|Không đủ vàng");
                        }
                    } else {
                        sendMessage("BUY_SKILL_FAIL|Không đủ vàng");
                    }
                }
                break;
            }

            case "USE_LIGHT_SKILL" -> {
                if (room != null) {
                    room.useLightSkill(this);
                }
                UserDAO userDao = new UserDAO();
                boolean success = userDao.useSkill(username, "light");
                if (success) {
                    sendMessage("SKILL_USED|light");
                    Map<String, Integer> skills = userDao.getUserSkills(username);
                    sendMessage("SKILL_UPDATE|light|" + skills.get("light"));
                }
            }

            case "USE_DARK_SKILL" -> {
                if (room != null) {
                    room.useDarkSkill(this);
                }
                UserDAO userDao = new UserDAO();
                boolean success = userDao.useSkill(username, "dark");
                if (success) {
                    sendMessage("SKILL_USED|dark");
                    Map<String, Integer> skills = userDao.getUserSkills(username);
                    sendMessage("SKILL_UPDATE|dark|" + skills.get("dark"));
                }
            }

            case "USE_FREEZE_SKILL" -> {
                if (room != null) {
                    room.useFreezeSkill(this);
                }
                UserDAO userDao = new UserDAO();
                boolean success = userDao.useSkill(username, "freeze");
                if (success) {
                    sendMessage("SKILL_USED|freeze");
                    Map<String, Integer> skills = userDao.getUserSkills(username);
                    sendMessage("SKILL_UPDATE|freeze|" + skills.get("freeze"));
                }
            }

            case "CLAIM_VIDEO_REWARD" -> {
                int rewardAmount = 1000;
                UserDAO userDao = new UserDAO();
                boolean success = userDao.addGold(username, rewardAmount);

                if (success) {
                    int newGold = userDao.getUserGold(username);
                    sendMessage("VIDEO_REWARD_SUCCESS|" + rewardAmount + "|" + newGold);
                    System.out.println("User " + username + " claimed video reward: +" + rewardAmount + " gold");
                } else {
                    sendMessage("VIDEO_REWARD_FAIL|Không thể cộng vàng");
                }
            }

            case "GET_LEADERBOARD" -> {
                System.out.println("Received GET_LEADERBOARD from client");

                StatsDAO statsDAO = new StatsDAO();
                List<Map<String, Object>> leaderboard = statsDAO.getLeaderboard();
                System.out.println("Leaderboard size: " + leaderboard.size());

                sendLeaderboard(leaderboard);
            }

            case "GET_MY_RANK" -> {
                if (username != null) {
                    UserDAO userDao = new UserDAO();
                    User user = userDao.getUserByUsername(username);
                    if (user != null) {
                        StatsDAO statsDAO = new StatsDAO();
                        int rank = statsDAO.getUserRank(user.getID());
                        sendMessage("MY_RANK|" + rank);
                    }
                }
            }

            default -> sendError("Lệnh không hỗ trợ: " + command);
        }
    }

    private void sendLeaderboard(List<Map<String, Object>> leaderboard) {
        if (leaderboard == null || leaderboard.isEmpty()) {
            sendMessage("LEADERBOARD_EMPTY");
            System.out.println("Sent empty leaderboard");
            return;
        }

        sendMessage("LEADERBOARD_COUNT|" + leaderboard.size());

        for (Map<String, Object> player : leaderboard) {
            String playerName = String.valueOf(player.get("playerName"));
            String avatar = String.valueOf(player.get("avatar"));
            String msg = "LEADERBOARD_PLAYER|"
                    + encode(playerName) + "|"
                    + encode(avatar) + "|"
                    + player.get("totalGames") + "|"
                    + player.get("wins") + "|"
                    + player.get("losses") + "|"
                    + player.get("draws") + "|"
                    + player.get("totalPoints") + "|"
                    + player.get("winRate");
            sendMessage(msg);
            System.out.println("Sent player: " + playerName);
        }

        sendMessage("LEADERBOARD_END");
    }

    private void handlePlayerInfo(String[] tokens) {
        if (tokens.length < 2)
            return;
        String username = decode(tokens[1]);
        this.username = username;

        UserDAO userDao = new UserDAO();
        User user = userDao.getUserByUsername(username);
        if (user != null) {
            this.playerName = user.getPlayerName();
            this.playerAvatar = user.getAvatar();
            System.out.println("Player registered for game: " + username + ", PlayerName: " + this.playerName);
            Map<String, Integer> skills = userDao.getUserSkills(username);
            String skillData = "SKILL_DATA|" +
                    skills.get("light") + "|" +
                    skills.get("dark") + "|" +
                    skills.get("freeze");
            sendMessage(skillData);
            System.out.println("Sent SKILL_DATA to client: " + skillData);
        } else {
            System.out.println("Player registered for game: " + username + " (not found in DB)");
            this.playerName = username;
        }
    }

    private void handleFindMatch() {
        if (username == null) {
            sendError("Chưa đăng nhập");
            return;
        }
        MatchMaker.addPlayer(this);
    }

    private void handleCancelMatch() {
        MatchMaker.removePlayer(this);
        sendMessage("CANCEL_OK");
    }

    private void handlePick(String[] tokens) {
        if (tokens.length < 2)
            return;
        if (room == null) {
            sendMessage("ERROR|Chưa vào phòng");
            return;
        }
        try {
            int number = Integer.parseInt(tokens[1].trim());
            System.out.println("HandlePick - number: " + number + " from player: " + getPlayername());
            room.handlePick(this, number);
        } catch (NumberFormatException e) {
            sendMessage("ERROR|Số không hợp lệ");
        }
    }

    private void handleLogin(String[] tokens) {
        if (tokens.length < 3) {
            sendError("Thiếu tham số đăng nhập.");
            return;
        }

        String username = decode(tokens[1]);
        String password = decode(tokens[2]);

        UserDAO userDao = new UserDAO();
        User user = userDao.login(username, password);
        if (user == null) {
            sendError("Tên đăng nhập hoặc mật khẩu không đúng.");
            return;
        }
        this.username = user.getUserName();
        this.playerName = user.getPlayerName();
        this.playerAvatar = user.getAvatar();

        System.out.println("User logged in - Username: " + this.username + ", PlayerName: " + this.playerName);

        String playerName = user.getPlayerName() == null ? "" : user.getPlayerName();
        String avatar = user.getAvatar() == null ? "" : user.getAvatar();
        String dob = user.getDob() == null ? "" : user.getDob().toString();

        sendSuccess(
                "LOGIN",
                String.valueOf(user.getID()),
                user.getUserName(),
                user.getFullName(),
                user.getGender(),
                dob,
                String.valueOf(user.getGold()),
                avatar,
                playerName);
        System.out.println("Sending login response - playerName: '" + playerName + "'");

    }

    public String getName() {
        return username == null || username.isBlank() ? "Anonymous" : username;
    }

    public String getUsername() {
        return getName();
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void send(String message) {
        sendMessage(message);
    }

    public void sendMessage(String message) {
        if (writer != null && !writer.checkError()) {
            writer.println(message);
        }
    }

    private void handleRegister(String[] tokens) {
        if (tokens.length < 6) {
            sendError("Thiếu tham số đăng ký.");
            return;
        }

        String username = decode(tokens[1]);
        String password = decode(tokens[2]);
        String fullName = decode(tokens[3]);
        String gender = decode(tokens[4]);
        String dobText = decode(tokens[5]);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullname(fullName);
        user.setGender(gender);
        if (!dobText.isBlank()) {
            user.setDob(Date.valueOf(LocalDate.parse(dobText)));
        }

        UserDAO userDao = new UserDAO();
        if (userDao.checkEmailExists(username)) {
            sendError("Email đã tồn tại.");
            return;
        }
        if (userDao.register(user)) {
            sendSuccess("REGISTER");
        } else {
            sendError("Đăng ký thất bại. Vui lòng thử lại sau.");
        }
    }

    private void handleResetPassword(String[] tokens) {
        if (tokens.length < 3) {
            sendError("Thiếu tham số đổi mật khẩu.");
            return;
        }

        String username = decode(tokens[1]);
        String newPassword = decode(tokens[2]);

        UserDAO userDao = new UserDAO();
        if (!userDao.checkEmailExists(username)) {
            sendError("Email chưa đăng ký.");
            return;
        }
        if (userDao.updatePassword(username, newPassword)) {
            sendSuccess("RESET_PASSWORD");
        } else {
            sendError("Cập nhật mật khẩu thất bại.");
        }
    }

    private void handleUpdateUserInfo(String[] tokens) {
        if (tokens.length < 7) {
            sendError("Thiếu tham số cập nhật thông tin.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(decode(tokens[1]));
        } catch (NumberFormatException e) {
            sendError("ID người dùng không hợp lệ.");
            return;
        }
        String username = decode(tokens[2]);
        String fullName = decode(tokens[3]);
        String gender = decode(tokens[4]);
        String dobText = decode(tokens[5]);
        String password = decode(tokens[6]);

        if (username.isEmpty() || fullName.isEmpty()) {
            sendError("Dữ liệu không hợp lệ.");
            return;
        }

        UserDAO userDao = new UserDAO();
        if (userDao.checkUsernameExistsExcluding(username, id)) {
            sendError("Email/Username đã được sử dụng bởi tài khoản khác.");
            return;
        }

        java.sql.Date dob = null;
        if (!dobText.isBlank()) {
            try {
                dob = Date.valueOf(LocalDate.parse(dobText));
            } catch (Exception e) {
                sendError("Ngày sinh không hợp lệ.");
                return;
            }
        }

        if (userDao.updateUserInfo(id, username, fullName, gender, dob, password)) {
            sendSuccess("UPDATE_USER_INFO");
        } else {
            sendError("Cập nhật thông tin thất bại.");
        }
    }

    private void handleUpdateProfile(String[] tokens) {
        if (tokens.length < 4) {
            sendError("Thiếu tham số cập nhật hồ sơ.");
            return;
        }

        String username = decode(tokens[1]);
        String playerName = decode(tokens[2]);
        String avatar = decode(tokens[3]);

        UserDAO userDao = new UserDAO();
        if (userDao.updateProfile(playerName, avatar, username)) {
            sendSuccess("UPDATE_PROFILE");
        } else {
            sendError("Cập nhật hồ sơ không thành công.");
        }
    }

    private void sendError(String message) {
        if (writer != null) {
            writer.println("ERROR|" + encode(message));
            writer.println("<END>");
        }
    }

    private void sendSuccess(String... parts) {
        if (writer != null) {
            StringBuilder builder = new StringBuilder("SUCCESS");
            for (String part : parts) {
                builder.append('|').append(encode(part == null ? "" : part));
            }
            writer.println(builder);
            writer.println("<END>");
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private void closeResources() {
        running.set(false);
        if (room != null) {
            room.checkPlayerDisconnected();
            room = null;
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {
        }
        if (writer != null) {
            writer.flush();
            writer.close();
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public String getPlayername() {
        if (playerName != null && !playerName.isEmpty()) {
            System.out.println("getPlayername() returning playerName: '" + playerName + "'");
            return playerName;
        }
        System.out.println("getPlayername() returning username: '" + username + "' (playerName is null/empty)");
        return username != null ? username : "Player";
    }

    public String getPlayerAvatar() {
        return playerAvatar != null && !playerAvatar.isEmpty()
                ? playerAvatar
                : "/icon/Martin-Berube-Character-Devil.256.png";
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPlayerAvatar(String avatar) {
        this.playerAvatar = avatar;
    }

    public boolean isDisconnected() {
        return !running.get() || writer == null || writer.checkError();
    }

    public Room getRoom() {
        return room;
    }

}
