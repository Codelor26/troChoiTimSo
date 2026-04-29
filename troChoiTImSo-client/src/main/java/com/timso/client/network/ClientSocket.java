package com.timso.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {
    private String host;
    private int port;
    private String dataToSend;
    private String serverResponse;

    public ClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setLoginData(String username, String password) {
        this.dataToSend = "LOGIN:" + username + ":" + password;
    }

    public String getServerResponse() {
        return serverResponse;
    }

    public void start() {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Da ket noi den server " + host + "tai port: " + port);
            startCommunication(socket);
        } catch (IOException e) {
            System.out.println("Loi ket noi den server " + e.getMessage());
        }
    }

    private void startCommunication(Socket socket) {
        try (Scanner scanner = new Scanner(System.in);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            writer.println(dataToSend);
            StringBuilder responseBuilder = new StringBuilder();
            String response;
            while ((response = reader.readLine()) != null) {
                if (response.equals("<END>"))
                    break;
                responseBuilder.append(response).append("\n");
            }
            this.serverResponse = responseBuilder.toString();
        } catch (IOException e) {
            System.out.println("Loi trong qua trinh giao tiep voi server " + e.getMessage());
        }
    }

}
