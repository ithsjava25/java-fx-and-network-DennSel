package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class ChatModelTest {

    @Test
    void sendMessageCallsConnectionWithMessageToSend() {
        // Arrange | Given
        var spy = new NtfyConnectionSpy();
        var model = new ChatModel(spy);
        model.setMessageToSend("testingtesting");
        // Act | When
        model.sendMessage();
        // Assert | Then
        assertThat(spy.message).isEqualTo("testingtesting");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var connection = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new ChatModel(connection);
        model.setMessageToSend("testing send");
        stubFor(post("/h8GikG9AWE9AG9jh3j3").willReturn(ok()));

        model.sendMessage();

        // Verifiera anropet
        verify(postRequestedFor(urlEqualTo("/h8GikG9AWE9AG9jh3j3"))
                .withRequestBody(containing("testing send")));
    }

    @Test
    void receiveMessageFromFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws InterruptedException {
        var connection = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new ChatModel(connection);

        stubFor(get("/h8GikG9AWE9AG9jh3j3/json?since=all")
                .willReturn(ok().withBody("{\"id\":\"1\",\"time\":123,\"event\":\"message\",\"topic\":\"chat\",\"message\":\"testing return\"}")));

        // Wait for message to arrive, or the test completes too early
        int maxWaitMs = 3000;
        int waited = 0;
        while (model.getMessages().isEmpty() && waited < maxWaitMs) {
            Thread.sleep(100);
            waited += 100;
        }

        verify(getRequestedFor(urlEqualTo("/h8GikG9AWE9AG9jh3j3/json?since=all")));

        assertFalse(model.getMessages().isEmpty());
        assertEquals("testing return", model.getMessages().getLast().message());
    }

    @Test
    void sendFileToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws IOException, InterruptedException {
        var connection = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new ChatModel(connection);

        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), "test file content");

        stubFor(post("/LPNqGAop0sFEfw0F").willReturn(ok()));
        stubFor(get("/LPNqGAop0sFEfw0F/json").willReturn(ok()));

        model.sendFile(tempFile);

        Thread.sleep(500);

        verify(postRequestedFor(urlEqualTo("/LPNqGAop0sFEfw0F"))
                .withHeader("Filnamn", equalTo(tempFile.getName()))
                .withRequestBody(containing("test file content")));
    }

    @Test
    void downloadFileFromFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws InterruptedException {
        var connection = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new ChatModel(connection);

        stubFor(get("/LPNqGAop0sFEfw0F/json").willReturn(ok()));

        String fileUrl = "http://localhost:" + wmRuntimeInfo.getHttpPort() + "/file/testfile.txt";
        byte[] fileContent = "downloaded file content".getBytes();

        stubFor(get("/file/testfile.txt")
                .willReturn(ok()
                        .withBody(fileContent)
                        .withHeader("Content-Type", "text/plain")));

        model.downloadFile(fileUrl);

        Thread.sleep(500);

        verify(getRequestedFor(urlEqualTo("/file/testfile.txt")));
    }
}