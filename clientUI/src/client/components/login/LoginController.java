package src.client.components.login;

import client.util.HttpUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import src.client.components.dashboardBody.DashboardBodyController;

import java.io.IOException;

public class LoginController {

    @FXML private TextField userNameTextField;
    @FXML private Label errorMessageLabel;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();
    private Stage primaryStage;

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void loginButtonClicked() {
        String username = userNameTextField.getText().trim();
        if (username.isEmpty()) {
            errorMessageProperty.set("Username cannot be empty");
            return;
        }

        // Server login endpoint
        String url = HttpUrl.parse("http://localhost:8080/semulator/login")
                .newBuilder()
                .addQueryParameter("username", username)
                .build()
                .toString();

        HttpUtils.getAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorMessageProperty.set("Server error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String body = response.body() != null ? response.body().string() : "<no body>";
                    Platform.runLater(() -> errorMessageProperty.set("Login failed: " + body));
                } else {
                    Platform.runLater(() -> switchToDashboard());
                }
            }
        });
    }

    private void switchToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/dashboardBody/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardBodyController controller = loader.getController();
            controller.setStage(primaryStage);
            controller.setActive();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
