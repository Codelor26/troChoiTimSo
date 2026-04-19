import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.json.JSONArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.LinkedHashMap;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String clientId; // ID sẽ được đặt sau khi client khởi tạo
    private PrintWriter writer;
    private BufferedReader reader;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public String getClientId() {
        return clientId;
    }

    // Gửi tin nhắn tới client mà handler này quản lý
    public void sendMessage(String message) {
        if (running.get() && writer != null && !writer.checkError()) {
            writer.println(message);
        }
    }

    private void closeResources() {
        running.set(false);
        System.out.println("Closing resources for: " + (clientId != null ? clientId : "unregistered"));
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            /* ignore */ }
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            /* ignore */ }
        try {
            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
        } catch (IOException e) {
            /* ignore */ }
        System.out.println("Resources closed for: " + (clientId != null ? clientId : "unregistered"));
    }

    @Override
    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Xử lý tin nhắn chính
            String messageFromClient;
            while (running.get() && (messageFromClient = reader.readLine()) != null) {
                // System.out.println("<- [" + this.clientId + "]: " + messageFromClient);
                messageFromClient = messageFromClient.trim();
                if ("bye".equalsIgnoreCase(messageFromClient.trim())) {
                    running.set(false);
                    sendMessage("SERVER: Bạn đang rời khỏi phòng chat...");
                    break;
                }
                if (messageFromClient.startsWith("weather ")) {
                    String location = messageFromClient.substring(8).trim();
                    writer.println(handleWeather(new StringTokenizer(location)));
                } else if (messageFromClient.startsWith("calc ")) {
                    String expression = messageFromClient.substring(5).trim();
                    writer.println(calculate(expression));
                } else {
                    sendMessage(
                            "Lỗi dữ liệu đầu vào!");
                }
            }

        } catch (SocketException e) {
            // Chỉ log nếu client chưa chủ động dừng (ví dụ: không phải do gửi 'bye')
            if (running.get()) {
                System.out.println("SocketException for " + (clientId != null ? clientId : clientInfo) + ": "
                        + e.getMessage() + ". Client disconnected?");
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println(
                        "IOException for " + (clientId != null ? clientId : clientInfo) + ": " + e.getMessage());
            }
        } finally {
            // Xóa client khỏi map và broadcast rời đi trước khi đóng tài nguyên
            bai3_3122410492_tranthinhuy_server.removeClient(this);
            // Đóng tài nguyên
            closeResources();
            System.out.println(
                    "Thread Handler của client " + (clientId != null ? clientId : clientInfo) + " đã kết thúc.");
        }
    }

    private String handleWeather(StringTokenizer tokenizer) {
        StringBuilder locationBuilder = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            locationBuilder.append(tokenizer.nextToken()).append(" ");
        }
        String location = locationBuilder.toString().trim();

        if (location.isEmpty())
            return "Lỗi: Thiếu địa danh";

        try {
            String apiKey = "66WYbfneIu1MFv16joeAqwB4OogsvvCb";
            String url = "https://api.tomorrow.io/v4/weather/realtime?location=" + location + "&apikey=" + apiKey;
            Document doc = Jsoup.connect(url).method(Connection.Method.GET).ignoreContentType(true).get();
            JSONObject json = new JSONObject(doc.body().text());

            JSONObject dataObj = json.getJSONObject("data");
            JSONObject valuesObj = dataObj.getJSONObject("values");
            double temp = valuesObj.getDouble("temperature");

            JSONObject locationObj = json.getJSONObject("location");
            String name = locationObj.getString("name");

            return "--> Chức năng tra nhiệt độ: " + name + " có nhiệt độ hiện tại là " + temp + " độ C.";
        } catch (Exception e) {
            return "Lỗi tra cứu thời tiết.";
        }
    }

    private String calculate(String expression) {
        try {
            String encodedExp = URLEncoder.encode(expression, "UTF-8");
            URL url = new URL("https://api.mathjs.org/v4/?expr=" + encodedExp);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = in.readLine();
            in.close();
            return "--> Kết quả phép tính: " + result;
        } catch (Exception e) {
            return "--> Phép tính có lỗi, không tính kết quả được.";
        }
    }

}
