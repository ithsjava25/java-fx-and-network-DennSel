package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load(); // Used for secret keys
        this.hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message, String topic) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .uri(URI.create(hostName + "/" + topic))
                .build();
        try {
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message!");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending message!");
        }
        return false;
    }

    @Override
    public void sendFile(File file, String topic) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            Path path = file.toPath();

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofFile(path))
                    .uri(URI.create(hostName + "/" + topic))
                    .header("Filnamn", file.getName())
                    .build();

            http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response ->
                            System.out.println("Fil skickad: " + response.statusCode())
                    )
                    .exceptionally(ex -> {
                        System.err.println("Fel vid filöverföring: " + ex.getMessage());
                        return null;
                    });

        } catch (IOException e) {
            System.err.println("Kunde inte läsa fil: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> receive(Consumer<NtfyMessageDto> messageHandler, String topic) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/" + topic + "/json"))
                .build();

        return http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .peek(jsonLine -> System.out.println("RAW JSON: " + jsonLine))
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.err.println("Kunde inte parsa meddelande: " + s);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(message -> message.event().equals("message"))
                        .peek(System.out::println) // Används för att, i konsolen, se vad som kommer in
                        .forEach(messageHandler));
    }

    @Override
    public void downloadFile(String fileUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(fileUrl))
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // Visa filväljare för att spara filen
                        Platform.runLater(() -> {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Spara fil");

                            // Försök extrahera filnamn från URL
                            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                            fileChooser.setInitialFileName(filename);

                            File saveFile = fileChooser.showSaveDialog(null);

                            if (saveFile != null) {
                                try {
                                    Files.write(saveFile.toPath(), response.body());
                                    System.out.println("Fil sparad: " + saveFile.getAbsolutePath());
                                } catch (IOException e) {
                                    System.err.println("Kunde inte spara fil: " + e.getMessage());
                                }
                            }
                        });
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Kunde inte ladda ner fil: " + ex.getMessage());
                    return null;
                });
    }
}
