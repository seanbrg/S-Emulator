package client.components.dashboardStage;

import client.components.login.LoginController;
import emulator.utils.WebConstants;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import client.components.availableUsers.AvailableUsersController;
import client.util.HttpUtils;
import java.io.Closeable;
import java.io.IOException;

public class DashboardStageController implements Closeable {

    @FXML
    private Label headerLabel;

    @FXML
    private BorderPane availableUsersComponent;

    @FXML
    private AvailableUsersController availableUsersComponentController;

    private Stage primaryStage;
    private final StringProperty currentUsername = new SimpleStringProperty();

    @FXML
    public void initialize() {
        if (headerLabel != null) {
            headerLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.concat("Welcome, ", currentUsername)
            );
        }

        // Initialize sub-controllers if needed
        if (availableUsersComponentController != null) {
            availableUsersComponentController.setDashboardController(this);
        }
    }

    public void setUsername(String username) {
        currentUsername.set(username);

        // Start refreshing available users
        if (availableUsersComponentController != null) {
            availableUsersComponentController.startUserListRefresher();
        }
    }

    public void logout() {
        System.out.println("Logging out user: " + currentUsername.get());

        HttpUtils.getAsync(WebConstants.LOGOUT_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Logout request failed: " + e.getMessage());
                switchToLogin();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                response.close();
                switchToLogin();
            }
        });
    }

    private void switchToLogin() {
        Platform.runLater(() -> {
            try {
                close();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/client/components/login/Login.fxml"));
                Parent root = loader.load();

                LoginController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);

                Scene scene = new Scene(root);
                String css = getClass().getResource("/client/resources/styles/style-dark.css").toExternalForm();
                scene.getStylesheets().add(css);

                primaryStage.setScene(scene);
                primaryStage.setTitle("S-Emulator");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void close() throws IOException {
        if (availableUsersComponentController != null) {
            availableUsersComponentController.close();
        }
    }
}