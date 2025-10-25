package client.components.dashboard.dashboardStage;

import client.components.dashboard.availableUsers.AvailableUsersController;
import client.components.header.HeaderController;
import client.components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class DashboardStageController {

    @FXML private HBox header;
    @FXML private HeaderController headerController;
    @FXML private BorderPane availableUsers;
    @FXML private AvailableUsersController availableUsersController;
    private MainAppController mainAppController;

    @FXML
    public void initialize() {
        //headerController.setMainController(this);
    }

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
    }

    @FXML
    private void handleLogout() {
        mainAppController.switchToLogin();
    }

    public void setActive(String userName) {
        availableUsersController.startListRefresher();
    }
}

