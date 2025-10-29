package client.components.login;

import client.components.mainApp.MainAppController;
import client.util.HttpUtils;
import emulator.utils.WebConstants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginController {

    @FXML private TextField userNameTextField;
    @FXML private Label errorMessageLabel;

    private MainAppController mainAppController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {

        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText().trim();

        if (userName.isEmpty()) {
            errorMessageProperty.set("User name cannot be empty.");
            return;
        }

        String encodedName = URLEncoder.encode(userName, StandardCharsets.UTF_8);
        String loginUrl = WebConstants.LOGIN_URL +
                "?" + WebConstants.USERNAME + "=" + encodedName;

        HttpUtils.getAsync(loginUrl).thenAccept(v -> {
            // Login successful
            Platform.runLater(() -> mainAppController.switchToDashboard(userName));
        }).exceptionally(ex -> {
            // Login failed
            Platform.runLater(() -> errorMessageProperty.set("Login failed: failed to connect to host."));
            return null;
        });
    }

    @FXML
    private void quitButtonClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
    }
}
