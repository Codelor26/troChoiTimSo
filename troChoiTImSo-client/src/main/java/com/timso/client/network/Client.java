package com.timso.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final AtomicBoolean running = new AtomicBoolean(true);

    // dùng để điều khiển cả 2 luồng dừng lại đồng bộ
    // Atomic giúp thread-safe (không lỗi khi 2 thread cùng sửa)

    public static void main(String[] args) {
        System.out.println("Đang kết nối đến server " + SERVER_ADDRESS + ":" + SERVER_PORT);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true); // autoFlush=true
                Scanner consoleScanner = new Scanner(System.in)) {
            // reader: đọc dữ liệu từ server
            // writer: gửi dữ liệu đến server
            // autoFlush = true: gửi là đẩy ngay, không cần flush tay
            System.out.println("Client đã kết nối đến server.");

            // Client cũng cần multithread --> tạo thread riêng để lắng nghe tin nhắn
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    // Chỉ lặp khi đang chạy & socket chưa bị đóng từ phía này
                    while (running.get() && !socket.isInputShutdown()) {
                        serverMessage = reader.readLine();
                        if (serverMessage == null) {
                            System.out.println("\n*** Server đã đóng kết nối. ***");
                            running.set(false); // Dừng luồng main nếu đang chạy
                            break;
                        }
                        System.out.println(serverMessage);
                        if (running.get()) { // Chỉ in dấu nhắc nếu client vẫn đang chạy
                            System.out.print("Nhập dữ liệu: ");
                        }
                    }
                } catch (SocketException e) {
                    // Xử lý lỗi khi socket bị đóng đột ngột hoặc có vấn đề mạng
                    if (running.get()) {
                        System.out.println("\n*** Kết nối đã bị đóng hoặc lỗi (SocketException). ***");
                        running.set(false); // Dừng luồng main
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("\n*** Lỗi khi đọc từ server: " + e.getMessage() + " ***");
                        running.set(false); // Dừng luồng main
                    }
                } finally {
                    running.set(false); // Đảm bảo luồng main cũng dừng khi listener kết thúc
                    System.out.println("\n*** Luồng lắng nghe kết thúc. ***");
                }
            });
            listenerThread.setDaemon(true);
            // Daemon thread sẽ tự động dừng khi tất cả thread non-daemon kết thúc, giúp
            // tránh trường hợp listener vẫn chạy khi main đã xong
            listenerThread.start();

            // Main thread gửi dữ liệu
            System.out.print("Nhập dữ liệu: ");
            while (running.get()) {
                // System.out.print("Nhập dữ liệu: ");
                if (!consoleScanner.hasNextLine()) {
                    running.set(false); // Trường hợp input bị đóng thì báo hiệu kết thúc luôn thread main
                    break;
                }
                String userInput = consoleScanner.nextLine();

                if (writer.checkError()) {
                    System.out.println("\n*** Lỗi khi gửi tin nhắn. Kết nối có thể đã mất. ***");
                    running.set(false); // Dừng cả hai luồng
                    break;
                }

                // Gửi tin nhắn
                writer.println(userInput);

                // Xử lý lệnh "bye"
                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Đang ngắt kết nối...");
                    running.set(false); // Đặt cờ dừng cho cả hai luồng
                    break;
                }
            }
        } catch (UnknownHostException ex) {
            System.err.println("Không tìm thấy server: " + SERVER_ADDRESS);
        } catch (IOException ex) {
            System.err.println("Lỗi kết nối: " + ex.getMessage());
        } finally {
            running.set(false); // Đảm bảo cờ được đặt false khi thoát
            System.out.println("\nClient đã đóng socket.");
        }
    }
}

// chưa xong
