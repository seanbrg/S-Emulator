package src.client.components.dashboardBody;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import src.client.components.avaliableUsers.AvaliableUsersController;

import java.io.Closeable;
import java.io.IOException;

public class DashboardBodyController implements Closeable {

    @FXML private VBox availableUsersComponent;
    @FXML private AvaliableUsersController availableUsersController;

    private Stage primaryStage;

    @FXML
    public void initialize() {
        availableUsersController.setDashboardController(this);
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setActive() {
        availableUsersController.startRefreshingUsers();
    }

    public void setInActive() {
        try {
            availableUsersController.stopRefreshingUsers();
        } catch (Exception ignored) {}
    }

    public void logout() {
        availableUsersController.logoutAndReturnToLogin();
    }

    @Override
    public void close() throws IOException {
        availableUsersController.stopRefreshingUsers();
    }
}
