package com.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class ChatController {
    private final ChatModel chatModel = new ChatModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;
    public Button selectFile;

    @FXML
    private TextField username;

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private TextField messageInput;

    @FXML
    private void initialize() {
        messageView.setItems(chatModel.getMessages());

        messageView.setCellFactory(lv -> new ListCell<NtfyMessageDto>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item.attachment() != null) {
                    Button downloadButton = new Button("📎 Ladda ner fil");
                    downloadButton.setOnAction(e -> chatModel.downloadFile(item.attachment().url()));

                    VBox box = new VBox(5);
                    box.getChildren().addAll(
                            new Label(item.message()),
                            downloadButton
                    );
                    setGraphic(box);
                    setText(null);
                } else {
                    setText(item.message());
                    setGraphic(null);
                }
            }
        });

        comboBox.setItems(FXCollections.observableArrayList("LPNqGAop0sFEfw0F", "h8GikG9AWE9AG9jh3j3"));
        comboBox.setValue("LPNqGAop0sFEfw0F");
        comboBox.setOnAction(e -> {
            chatModel.changeTopic(comboBox.getValue());
        });

    }

    public void sendMessage(ActionEvent actionEvent) {
        if (!messageInput.getText().isEmpty()) {
            String userName = "Anonymous";
            userName = username.getText();
            if (userName.isEmpty()) {
                userName = "Anonymous";
            }
            chatModel.setMessageToSend(userName + ": " + messageInput.getText());
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

    public void attachFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil");
        File selectedFile = fileChooser.showOpenDialog(selectFile.getScene().getWindow());
        if (selectedFile != null) {
            chatModel.sendFile(selectedFile);
        }
    }

}
