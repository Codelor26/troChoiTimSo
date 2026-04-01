package com.timso.server.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Room room;
    private String username;

    // Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Lỗi tạo I/O");
        }
    }

    // mỗi client chạy 1 thread
    public void run() {
        try {
            // yêu cầu username
            sendMessage("ENTER_USERNAME");
            username = in.readLine();

            System.out.println("User kết nối: " + username);

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(username + " gửi: " + msg);

                // click số
                if (msg.startsWith("CLICK:")) {
                    int number = Integer.parseInt(msg.split(":")[1]);

                    if (room != null) {
                        room.handleClick(this, number);
                    }
                }

                // skill che màn hình
                if (msg.equals("SKILL_HIDE")) {
                    if (room != null) {
                        room.useHideSkill(this);
                    }
                }
                // thoát
                if (msg.equalsIgnoreCase("bye")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Client ngắt kết nối: " + username);
        }
    }

    // gửi dữ liệu về client
    public void sendMessage(String msg) {
        out.println(msg);
    }

    // gán room
    public void setRoom(Room room) {
        this.room = room;
    }

    // lấy username
    public String getUsername() {
        return username;
    }
}
