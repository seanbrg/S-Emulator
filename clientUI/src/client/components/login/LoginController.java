package client.components.login;

import client.components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

        // Simulate a successful login
        Platform.runLater(() -> mainAppController.switchToDashboard(userName));
    }

    @FXML
    private void quitButtonClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
    }
}
