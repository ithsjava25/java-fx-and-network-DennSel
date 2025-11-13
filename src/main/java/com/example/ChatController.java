package com.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.stream.Collectors;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class ChatController {
    private final ChatModel chatModel = new ChatModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;
    public ListView<String> viewMessages;

    @FXML
    private TextField messageInput;

    @FXML
    private void initialize() {
        messageView.setItems(chatModel.getMessages());
    }

    public void sendMessage(ActionEvent actionEvent) {
        if (!messageInput.getText().isEmpty()) {
            chatModel.setMessageToSend(messageInput.getText());
            chatModel.sendMessage();
            messageInput.clear();
        }
    }

    public void focusTextField() {
        messageInput.requestFocus();
    }

    public void receiveMessage(ActionEvent actionEvent) {
        chatModel.receiveMessage();
    }
}
