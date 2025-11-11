package com.example;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChatModelTest {

    @Test
    void sendMessageCallsConnectionWithMessageToSend() {
        // Arrange/Given
        var spy = new NtfyConnectionSpy();
        var model = new ChatModel(spy);
        model.setMessageToSend("testingtesting");
        // Act/When
        model.sendMessage();
        // Assert/Then
        assertThat(spy.message).isEqualTo("testingtesting");
    }
}