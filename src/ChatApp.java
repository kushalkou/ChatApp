import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Set up the GUI layout
        VBox layout = new VBox(10);

        // Add a text field for input
        TextField messageField = new TextField();
        messageField.setPromptText("Enter your message");

        // Add a button to send the message
        Button sendButton = new Button("Send");

        // Set an action on the send button (For now, just print the message)
        sendButton.setOnAction(event -> {
            String message = messageField.getText();
            System.out.println("Message sent: " + message);
            messageField.clear();
        });

        // Add elements to the layout
        layout.getChildren().addAll(messageField, sendButton);

        // Set up the scene
        Scene scene = new Scene(layout, 300, 200);

        // Set up the stage
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);  // Launch the JavaFX application
    }
}
