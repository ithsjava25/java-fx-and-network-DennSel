package com.example;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {

    boolean send(String message, String topic);

    void sendFile(File file, String topic);

    CompletableFuture<Void> receive(Consumer<NtfyMessageDto> messageHandler, String topic);

    void downloadFile(String fileUrl);
}
