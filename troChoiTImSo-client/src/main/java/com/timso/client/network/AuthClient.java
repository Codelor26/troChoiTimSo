package com.timso.client.network;

import com.timso.common.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthClient {
    private final String host;
    private final int port;
    private String lastError;

    public AuthClient() {
        this("localhost", 12345);
    }

    public AuthClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getLastError() {
        return lastError;
    }

    public User login(String username, String password) {
        String request = buildCommand("LOGIN", username, password);
        List<String> response = sendRequest(request);
        if (response == null || response.isEmpty()) {
            lastError = "Server không trả về dữ liệu.";
            return null;
        }
        String[] payload = response.get(0).split("\\|", -1);
        if (payload.length == 0) {
            lastError = "Phản hồi không hợp lệ từ server.";
            return null;
        }

        if ("SUCCESS".equalsIgnoreCase(payload[0])) {
            if (payload.length < 10) {
                lastError = "Định dạng trả về của server không đúng.";
                return null;
            }
            String commandName = decode(payload[1]);
            if (!"LOGIN".equalsIgnoreCase(commandName)) {
                lastError = "Định dạng trả về của server không đúng.";
                return null;
            }
            return parseUser(payload);
        }

        lastError = buildErrorMessage(payload);
        return null;
    }

    public boolean register(User user) {
        String request = buildCommand("REGISTER", user.getUserName(), user.getPassword(), user.getFullName(),
                user.getGender(), user.getDob() == null ? "" : user.getDob().toString());
        List<String> response = sendRequest(request);
        if (response == null || response.isEmpty()) {
            lastError = "Server không trả về dữ liệu.";
            return false;
        }
        String[] payload = response.get(0).split("\\|", -1);
        if (payload.length == 0) {
            lastError = "Phản hồi không hợp lệ từ server.";
            return false;
        }
        if ("SUCCESS".equalsIgnoreCase(payload[0]) && payload.length > 1) {
            String commandName = decode(payload[1]);
            if ("REGISTER".equalsIgnoreCase(commandName)) {
                return true;
            }
        }
        lastError = buildErrorMessage(payload);
        return false;
    }

    public boolean resetPassword(String username, String newPassword) {
        String request = buildCommand("RESET_PASSWORD", username, newPassword);
        List<String> response = sendRequest(request);
        if (response == null || response.isEmpty()) {
            lastError = "Server không trả về dữ liệu.";
            return false;
        }
        String[] payload = response.get(0).split("\\|", -1);
        if (payload.length == 0) {
            lastError = "Phản hồi không hợp lệ từ server.";
            return false;
        }
        if ("SUCCESS".equalsIgnoreCase(payload[0]) && payload.length > 1) {
            String commandName = decode(payload[1]);
            if ("RESET_PASSWORD".equalsIgnoreCase(commandName)) {
                return true;
            }
        }
        lastError = buildErrorMessage(payload);
        return false;
    }

    public boolean updateProfile(String username, String playerName, String avatar) {
        String request = buildCommand("UPDATE_PROFILE", username, playerName, avatar);
        List<String> response = sendRequest(request);
        if (response == null || response.isEmpty()) {
            lastError = "Server không trả về dữ liệu.";
            return false;
        }
        String[] payload = response.get(0).split("\\|", -1);
        if (payload.length == 0) {
            lastError = "Phản hồi không hợp lệ từ server.";
            return false;
        }
        if ("SUCCESS".equalsIgnoreCase(payload[0]) && payload.length > 1) {
            String commandName = decode(payload[1]);
            if ("UPDATE_PROFILE".equalsIgnoreCase(commandName)) {
                return true;
            }
        }
        lastError = buildErrorMessage(payload);
        return false;
    }

    public boolean updateUserInfo(int id, String username, String fullName, String gender, String dob, String password) {
        String request = buildCommand("UPDATE_USER_INFO",
                String.valueOf(id),
                username,
                fullName,
                gender,
                dob,
                password);
        List<String> response = sendRequest(request);
        if (response == null || response.isEmpty()) {
            lastError = "Server không trả về dữ liệu.";
            return false;
        }
        String[] payload = response.get(0).split("\\|", -1);
        if (payload.length == 0) {
            lastError = "Phản hồi không hợp lệ từ server.";
            return false;
        }
        if ("SUCCESS".equalsIgnoreCase(payload[0]) && payload.length > 1) {
            String commandName = decode(payload[1]);
            if ("UPDATE_USER_INFO".equalsIgnoreCase(commandName)) {
                return true;
            }
        }
        lastError = buildErrorMessage(payload);
        return false;
    }

    private List<String> sendRequest(String request) {
        try (Socket socket = new Socket(host, port);
                PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer.println(request);
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("<END>".equals(line)) {
                    break;
                }
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            lastError = "Không thể kết nối tới server: " + e.getMessage();
            return null;
        }
    }

    private String buildCommand(String command, String... args) {
        StringBuilder builder = new StringBuilder(command);
        for (String value : args) {
            builder.append('|').append(encode(value == null ? "" : value));
        }
        return builder.toString();
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

    private User parseUser(String[] payload) {
        if (payload.length < 10) {
            return null;
        }
        try {
            User user = new User();
            user.setId(Integer.parseInt(decode(payload[2])));
            user.setUsername(decode(payload[3]));
            user.setFullname(decode(payload[4]));
            user.setGender(decode(payload[5]));
            String dobValue = decode(payload[6]);
            if (!dobValue.isBlank()) {
                user.setDob(Date.valueOf(dobValue));
            }
            user.setGold(Integer.parseInt(decode(payload[7])));
            user.setAvatar(decode(payload[8]));
            user.setPlayerName(decode(payload[9]));
            return user;
        } catch (NumberFormatException e) {
            lastError = "Lỗi: " + e.getMessage();
            return null;
        } catch (Exception e) {
            lastError = "Không thể xử lý dữ liệu người dùng từ server.";
            return null;
        }
    }

    private String buildErrorMessage(String[] payload) {
        if (payload.length >= 2) {
            return decode(payload[1]);
        }
        return "Lỗi không xác định từ server.";
    }
}
