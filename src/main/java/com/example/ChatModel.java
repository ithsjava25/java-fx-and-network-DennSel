package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.RunOnFx.runOnFx;

/**
 * Model layer: encapsulates application data and business logic.
 */

public class ChatModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private CompletableFuture<Void> currentSubscription;

    private String topic = "LPNqGAop0sFEfw0F";


    public ChatModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public void changeTopic(String newTopic) {

        if (currentSubscription != null && !currentSubscription.isDone()) {
            currentSubscription.cancel(true);
        }

        this.topic = newTopic;
        messages.clear();
        receiveMessage();
    }

    /* ************MESSAGES***************** */

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    public void sendMessage() {
        connection.send(getMessageToSend(), topic);
    }

    public void receiveMessage() {
        currentSubscription = connection.receive(m -> runOnFx(() -> handleIncomingMessage(m)), topic, null);
    }

    private void handleIncomingMessage(NtfyMessageDto m) {
        if (m.event().equals("message")) {
            messages.add(m);
        }
    }

    /* ************FILES***************** */

    public void sendFile(File file) {
        connection.sendFile(file, topic);
    }

    public void downloadFile(String fileUrl) {
        connection.downloadFile(fileUrl);
    }
}

