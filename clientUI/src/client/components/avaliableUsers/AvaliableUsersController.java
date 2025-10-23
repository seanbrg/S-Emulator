package src.client.components.avaliableUsers;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import src.client.components.dashboardBody.DashboardBodyController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class AvaliableUsersController {

    @FXML private ListView<String> usersListView;

    private DashboardBodyController dashboardController;

    public void setDashboardController(DashboardBodyController controller) {
        this.dashboardController = controller;
    }

    public void startRefreshingUsers() {
        refreshUsers();
    }

    public void stopRefreshingUsers() {
        // stop scheduled refresh if added
    }

    private void refreshUsers() {
        HttpUtils.getAsync("http://localhost:8080/semulator/users", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Failed to load users: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) return;
                String body = response.body().string();
                Type type = new TypeToken<List<String>>(){}.getType();
                List<String> users = new Gson().fromJson(body, type);
                Platform.runLater(() -> usersListView.getItems().setAll(users));
            }
        });
    }

    public void logoutAndReturnToLogin() {
        HttpUtils.getAsync("http://localhost:8080/semulator/logout", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Logout failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                Platform.runLater(() -> {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("/client/components/login/Login.fxml")
                        );
                        javafx.scene.Parent root = loader.load();
                        Stage stage = (Stage) usersListView.getScene().getWindow();
                        stage.setScene(new javafx.scene.Scene(root));
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}

