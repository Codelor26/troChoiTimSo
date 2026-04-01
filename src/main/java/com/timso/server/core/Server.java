package com.timso.server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;

    // hàng chờ người chơi
    private static List<ClientHandler> waitingPlayers = new ArrayList<>();

    // Constructor
    public Server(int port) {
        this.port = port;
    }

    // khởi động server
    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server đang lắng nghe tại port " + port);

            while (true) {
                Socket socket = server.accept(); // chờ client
                handleClient(socket);
            }

        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    // xử lý khi có client kết nối
    private void handleClient(Socket socket) {
        System.out.println("Client kết nối: " + socket.getRemoteSocketAddress());

        // tạo thread cho mỗi client
        ClientHandler client = new ClientHandler(socket);
        client.start();

        // đưa vào hàng chờ
        synchronized (waitingPlayers) {
            waitingPlayers.add(client);
            System.out.println("Số người đang chờ: " + waitingPlayers.size());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(12345);
        server.start();
    }
}
