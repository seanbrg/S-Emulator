package client.components.dashboard;

import client.components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label headerLabel;
    private MainAppController mainAppController;

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
    }

    public void updateHeader(String text) {
        headerLabel.setText(text);
    }

    @FXML
    private void handleLogout() {
        mainAppController.switchToLogin();
    }
}

