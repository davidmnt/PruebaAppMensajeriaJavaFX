package com.example.pruebaappmensajeria;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor extends Application {
    private TextArea logArea;
    private List<PrintWriter> clientWriters;

    @Override
    public void start(Stage primaryStage) {
        logArea = new TextArea();
        logArea.setEditable(false);

        TextField messageField = new TextField();
        messageField.setPromptText("Type a message...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessageToAllClients(messageField.getText()));

        HBox inputBox = new HBox(messageField, sendButton);

        VBox root = new VBox(logArea, inputBox);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Server");
        primaryStage.show();

        clientWriters = new ArrayList<>();
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(5000);
                Platform.runLater(() -> log("Server started. Waiting for client..."));

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    Platform.runLater(() -> log("Client connected: " + clientSocket.getInetAddress()));

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientWriters.add(writer);

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    new Thread(() -> {
                        try {
                            String line;
                            while ((line = in.readLine()) != null) {
                                final String finalLine = line;
                                Platform.runLater(() -> log("Received from " + clientSocket.getInetAddress() + ": " + finalLine));
                            }
                        } catch (IOException e) {
                            Platform.runLater(() -> log("Error reading from client: " + e.getMessage()));
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                Platform.runLater(() -> log("Error closing client socket: " + e.getMessage()));
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                Platform.runLater(() -> log("Error starting server: " + e.getMessage()));
            }
        }).start();
    }

    private void sendMessageToAllClients(String message) {
        Platform.runLater(() -> log("Server: " + message));
        for (PrintWriter writer : clientWriters) {
            writer.println("Server: " + message);
        }
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}