package com.example;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    String topic;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public boolean send(String message, String topic) {
        this.message = message;
        this.topic = topic;
        return true;
    }

    @Override
    public void sendFile(File file, String topic) {

    }

    @Override
    public CompletableFuture<Void> receive(Consumer<NtfyMessageDto> messageHandler, String topic, Long sinceTime) {
        return null;
    }

    @Override
    public void downloadFile(String fileUrl) {

    }
}
