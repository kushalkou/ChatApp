import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI Elements
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        Label messageLabel = new Label();

        // Button actions
        loginButton.setOnAction(_ -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.equals("user") && password.equals("pass")) {
                messageLabel.setText("Login successful!");
            } else {
                messageLabel.setText("Invalid credentials.");
            }
        });

        registerButton.setOnAction(_ -> {
            messageLabel.setText("Register clicked (add DB logic later)");
        });

        VBox layout = new VBox(10, usernameField, passwordField, loginButton, registerButton, messageLabel);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setTitle("Login/Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
