package client.components.dashboard.dashboardStage;

import client.components.dashboard.availableUsers.AvailableUsersController;
import client.components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class DashboardStageController {

    @FXML private Label headerLabel;
    @FXML private BorderPane availableUsers;
    @FXML private AvailableUsersController availableUsersController;
    private MainAppController mainAppController;

    @FXML
    public void initialize() {
    }

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

    public void setActive(String userName) {
        updateHeader("Welcome, " + userName + "!");
        availableUsersController.startListRefresher();
    }
}

