package client.components.availableUsers;

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
                List<String> users = AvailableUsersFetcher.fetchUsers(); // fetch dummy list
                usersListUpdater.accept(users);
                httpStatusConsumer.accept("Available users updated.");
            }
        }
    }

    public static class AvailableUsersFetcher {

        private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        /**
         * Fetch users from server (dummy data for now)
         */
        public static List<String> fetchUsers() {
            // TODO: Replace with actual server request
            return Arrays.asList("Alice", "Bob", "Charlie", "David");
        }

        /**
         * Async fetch (example)
         */
        public static void fetchUsersAsync(String url, Consumer<List<String>> onResult) {
            Request request = new Request.Builder().url(url).build();
            HTTP_CLIENT.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onResult.accept(Collections.emptyList());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        List<String> users = Arrays.asList(body.replace("[","").replace("]","")
                                .replace("\"","").split(","));
                        onResult.accept(users);
                    } else {
                        onResult.accept(Collections.emptyList());
                    }
                }
            });
        }
    }
}
