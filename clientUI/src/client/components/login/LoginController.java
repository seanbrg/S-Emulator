package client.components.login;

import emulator.utils.WebConstants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import client.components.dashboardBody.DashboardBodyController;
import client.util.HttpUtils;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField userNameTextField;

    @FXML
    private Label errorMessageLabel;

    private Stage primaryStage;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);

        // Optional: log cookies or HTTP info
        HttpUtils.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() -> System.out.println("Cookie: " + line))
        );
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();

        // Validate input
        if (userName == null || userName.trim().isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        // Build the login URL
        String finalUrl = HttpUrl
                .parse(WebConstants.LOGIN_URL)
                .newBuilder()
                .addQueryParameter(WebConstants.USERNAME, userName.trim())
                .build()
                .toString();

        System.out.println("Attempting login for: " + userName);

        // Send async HTTP GET
        HttpUtils.getAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Connection failed: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.code() == 200) {
                    // Login succeeded — switch to dashboard
                    Platform.runLater(() -> {
                        try {
                            switchToDashboard(userName.trim());
                        } catch (IOException e) {
                            errorMessageProperty.set("Failed to load dashboard: " + e.getMessage());
                        }
                    });
                } else {
                    // Show error message from server
                    Platform.runLater(() ->
                            errorMessageProperty.set("Login failed: " + responseBody)
                    );
                }

                response.close();
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }

    private void switchToDashboard(String username) throws IOException {
        // Load dashboard FXML
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/client/components/dashboardBody/DashboardBody.fxml"));
        Parent root = loader.load();

        // Initialize dashboard controller
        DashboardBodyController controller = loader.getController();
        controller.setUsername(username);
        controller.setPrimaryStage(primaryStage);

        // Create and apply new scene
        Scene scene = new Scene(root);
        String css = getClass().getResource("/client/resources/styles/style-dark.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Show dashboard
        primaryStage.setScene(scene);
        primaryStage.setTitle("S-Emulator - " + username);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
