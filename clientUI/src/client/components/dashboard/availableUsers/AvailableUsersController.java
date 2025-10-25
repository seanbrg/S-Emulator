package client.components.dashboard.availableUsers;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import okhttp3.*;



import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static client.util.Constants.REFRESH_RATE;

public class AvailableUsersController implements Closeable {

    // ---------------------- FXML ----------------------
    @FXML private ListView<String> usersListView;
    @FXML private Label usersHeaderLabel;

    // ---------------------- Properties ----------------------
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalUsers;
    private HttpStatusUpdate httpStatusUpdate;

    // ---------------------- Timer ----------------------
    private Timer timer;
    private TimerTask listRefresher;

    public AvailableUsersController() {
        autoUpdate = new SimpleBooleanProperty(true);
        totalUsers = new SimpleIntegerProperty(0);
    }

    @FXML
    public void initialize() {
        usersHeaderLabel.textProperty().bind(Bindings.concat("Available Users: (", totalUsers.asString(), ")"));
    }

    public void setHttpStatusUpdate(HttpStatusUpdate httpStatusUpdate) {
        this.httpStatusUpdate = httpStatusUpdate;
    }

    public BooleanProperty autoUpdatesProperty() {
        return autoUpdate;
    }

    // ---------------------- Update Logic ----------------------
    private void updateUsersList(List<String> usersNames) {
        Platform.runLater(() -> {
            ObservableList<String> items = usersListView.getItems();
            items.clear();
            items.addAll(usersNames);
            totalUsers.set(usersNames.size());
        });
    }

    public void startListRefresher() {
        listRefresher = new UserListRefresher(
                autoUpdate,
                httpStatusUpdate != null ? httpStatusUpdate::updateHttpLine : s -> {},
                this::updateUsersList
        );
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    @Override
    public void close() {
        usersListView.getItems().clear();
        totalUsers.set(0);
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }

    // ====================== Utilities ======================

    @FunctionalInterface
    public interface HttpStatusUpdate {
        void updateHttpLine(String line);
    }

    public static class UserListRefresher extends TimerTask {
        private final BooleanProperty autoUpdate;
        private final Consumer<String> httpStatusConsumer;
        private final Consumer<List<String>> usersListUpdater;
        private static final Gson GSON = new Gson();

        public UserListRefresher(BooleanProperty autoUpdate,
                                 Consumer<String> httpStatusConsumer,
                                 Consumer<List<String>> usersListUpdater) {
            this.autoUpdate = autoUpdate;
            this.httpStatusConsumer = httpStatusConsumer;
            this.usersListUpdater = usersListUpdater;
        }

        @Override
        public void run() {
            if (autoUpdate.get()) {
                httpStatusConsumer.accept("Updating available users...");

                HttpUtils.getAsync(WebConstants.USERS_URL).thenAccept(json -> {
                    // Parse JSON array of strings
                    try {
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        List<String> usersList = GSON.fromJson(json, listType);

                        usersListUpdater.accept(usersList);
                        httpStatusConsumer.accept("Available users updated.");

                    } catch (Exception e) {
                        httpStatusConsumer.accept("Failed to parse users list: " + e.getMessage());
                        return;
                    }
                }).exceptionally(ex -> {
                    httpStatusConsumer.accept("Failed to update users: " + ex.getCause().getMessage());
                    return null;
                });
            }
        }
    }
}
