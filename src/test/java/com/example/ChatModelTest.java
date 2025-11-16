package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

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
        stubFor(post("/LPNqGAop0sFEfw0F").willReturn(ok()));

        model.sendMessage();

        // Verifiera anropet
        verify(postRequestedFor(urlEqualTo("/LPNqGAop0sFEfw0F"))
                .withRequestBody(containing("testing send")));
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