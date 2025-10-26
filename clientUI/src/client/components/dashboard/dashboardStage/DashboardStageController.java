package client.components.dashboard.dashboardStage;

import client.components.dashboard.availableUsers.AvailableUsersController;
import client.components.dashboard.availablePrograms.AvailableProgramsController;
import client.components.dashboard.dashboardHeader.DashboardHeaderController;
import client.components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class DashboardStageController {

    @FXML private HBox header;
    @FXML private DashboardHeaderController headerController;

    @FXML private BorderPane availableUsers;
    @FXML private AvailableUsersController availableUsersController;

    @FXML private BorderPane availablePrograms;
    @FXML private AvailableProgramsController availableProgramsController;

    private MainAppController mainAppController;

    @FXML
    public void initialize() {
        // Set up callback to refresh programs table when a new program is uploaded
        if (headerController != null && availableProgramsController != null) {
            headerController.setOnProgramUploadedCallback(() -> {
                // Force immediate refresh of programs table
                availableProgramsController.startListRefresher();
            });
        }
    }

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
    }

    @FXML
    private void handleLogout() {

        if (availableProgramsController != null) {
            availableProgramsController.stopListRefresher();
        }

        mainAppController.switchToLogin();
    }

    public void setActive(String userName) {
        // Start users refresher
        if (availableUsersController != null) {
            availableUsersController.startListRefresher();
        }

        // Start programs refresher
        if (availableProgramsController != null) {
            availableProgramsController.startListRefresher();
        }

        // Show username in header
        // not working
        if (headerController != null) {
            headerController.setUserName(userName);
        }
    }
}