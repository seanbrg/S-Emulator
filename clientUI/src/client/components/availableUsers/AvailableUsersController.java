package client.components.availableUsers;

import com.google.gson.Gson;



import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import client.components.dashboardStage.dashboardStageController;
import client.util.HttpUtils;


import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AvailableUsersController implements Closeable {

    @FXML
    private ListView<String> usersListView;

    @FXML
    private Button unselectUserButton;

    private dashboardStageController dashboardController;
    private Timer timer;
    private TimerTask userListRefresher;
    private final ObservableList<String> usersList = FXCollections.observableArrayList();
    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        if (usersListView != null) {
            usersListView.setItems(usersList);
        }

        if (unselectUserButton != null) {
            unselectUserButton.setOnAction(event -> handleUnselectUser());
        }
    }

    public void startUserListRefresher() {
        userListRefresher = new TimerTask() {
            @Override
            public void run() {
                refreshUsersList();
            }
        };
        timer = new Timer(true);
        timer.scheduleAtFixedRate(userListRefresher, 0, 2000); // Refresh every 2 seconds
    }

    private void refreshUsersList() {
        HttpUtils.getAsync(WebConstants.USERS_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to fetch users list: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> users = gson.fromJson(responseBody, listType);

                    Platform.runLater(() -> {
                        usersList.clear();
                        usersList.addAll(users);
                    });
                }
                response.close();
            }
        });
    }

    private void handleUnselectUser() {
        usersListView.getSelectionModel().clearSelection();
    }

    public void setDashboardController(dashboardStageController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void close() throws IOException {
        if (userListRefresher != null) {
            userListRefresher.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }
}
