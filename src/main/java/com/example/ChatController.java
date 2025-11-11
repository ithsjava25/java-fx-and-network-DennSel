package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class ChatController {

    // FIXA TILL ICKE-NULL
    private final ChatModel chatModel = new ChatModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private TextField messageInput;

    @FXML
    private void initialize() {
        messageView.setItems(chatModel.getMessages());
    }

    public void sendMessage(ActionEvent actionEvent) {
        chatModel.setMessageToSend(messageInput.getText());
        chatModel.sendMessage();
        messageInput.clear();
    }

    public void focusTextField() {
        messageInput.requestFocus();
    }
}
