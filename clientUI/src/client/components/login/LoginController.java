package src.client.components.login;

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
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import src.client.components.dashboardBody.DashboardBodyController;
import emulator.utils.HttpClientUtil;

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
        if (errorMessageLabel != null) {
            errorMessageLabel.textProperty().bind(errorMessageProperty);
        }

        HttpClientUtil.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() -> System.out.println("Cookie: " + line))
        );
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String userName = userNameTextField.getText();

        if (userName == null || userName.trim().isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(WebConstants.LOGIN_URL)
                .newBuilder()
                .addQueryParameter(WebConstants.USERNAME, userName.trim())
                .build()
                .toString();

        System.out.println("Attempting login for: " + userName);

        HttpClientUtil.runAsync(finalUrl, new Callback() {
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
                    Platform.runLater(() -> {
                        try {
                            switchToDashboard(userName.trim());
                        } catch (IOException e) {
                            errorMessageProperty.set("Failed to load dashboard: " + e.getMessage());
                        }
                    });
                } else {
                    Platform.runLater(() ->
                            errorMessageProperty.set("Login failed: " + responseBody)
                    );
                }
                response.close();
            }
        });
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        Platform.exit();
    }

    private void switchToDashboard(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/client/components/dashboardBody/DashboardBody.fxml"));
        Parent root = loader.load();

        DashboardBodyController controller = loader.getController();
        controller.setUsername(username);
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root);
        String css = getClass().getResource("/client/resources/styles/style-dark.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setScene(scene);
        primaryStage.setTitle("S-Emulator - " + username);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}