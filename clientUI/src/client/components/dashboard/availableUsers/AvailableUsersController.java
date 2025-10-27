package client.components.dashboard.availableUsers;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import users.UserDashboard;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

import static client.util.Constants.REFRESH_RATE;

public class AvailableUsersController implements Closeable {

    // ---------------------- FXML ----------------------
    @FXML
    private TableView<UserDashboard> usersTableView;
    @FXML
    private TableColumn<UserDashboard, String> userNameColumn;
    @FXML
    private TableColumn<UserDashboard, Integer> mainProgramsColumn;
    @FXML
    private TableColumn<UserDashboard, Integer> subfunctionsColumn;
    @FXML
    private TableColumn<UserDashboard, Integer> currentCreditsColumn;
    @FXML
    private TableColumn<UserDashboard, Integer> creditsUsedColumn;
    @FXML
    private TableColumn<UserDashboard, Integer> runsColumn;
    @FXML
    private Label usersHeaderLabel;
    @FXML
    private Button unselectUserButton;

    // ---------------------- Properties ----------------------
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalUsers;
    private HttpStatusUpdate httpStatusUpdate;

    // ---------------------- Timer ----------------------
    private Timer timer;
    private TimerTask listRefresher;

    // ---------------------- Observable List ----------------------
    private final ObservableList<UserDashboard> usersList = FXCollections.observableArrayList();

    public AvailableUsersController() {
        autoUpdate = new SimpleBooleanProperty(true);
        totalUsers = new SimpleIntegerProperty(0);
    }

    @FXML
    public void initialize() {


        // Bind header label to show total users count
        usersHeaderLabel.textProperty().bind(
                Bindings.concat("Available Users (", totalUsers.asString(), ")")
        );

        // Setup table columns
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        mainProgramsColumn.setCellValueFactory(new PropertyValueFactory<>("mainProgramsUploaded"));
        subfunctionsColumn.setCellValueFactory(new PropertyValueFactory<>("subfunctionsContributed"));
        currentCreditsColumn.setCellValueFactory(new PropertyValueFactory<>("currentCredits"));
        creditsUsedColumn.setCellValueFactory(new PropertyValueFactory<>("creditsUsed"));
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfRuns"));

        // Set the observable list to the table
        usersTableView.setItems(usersList);

        // Setup unselect button
        if (unselectUserButton != null) {
            unselectUserButton.setOnAction(event -> usersTableView.getSelectionModel().clearSelection());
        }


    }

    public void setHttpStatusUpdate(HttpStatusUpdate httpStatusUpdate) {
        this.httpStatusUpdate = httpStatusUpdate;
    }

    public BooleanProperty autoUpdatesProperty() {
        return autoUpdate;
    }

    // ---------------------- Update Logic ----------------------
    private void updateUsersList(List<UserDashboard> usersData) {


        Platform.runLater(() -> {
            usersList.clear();
            if (usersData != null && !usersData.isEmpty()) {
                usersList.addAll(usersData);
                totalUsers.set(usersData.size());


            } else {
                totalUsers.set(0);

            }
        });
    }

    public void startListRefresher() {

        listRefresher = new UserListRefresher(
                autoUpdate,
                httpStatusUpdate != null ? httpStatusUpdate::updateHttpLine : s -> {
                },
                this::updateUsersList
        );
        timer = new Timer();
        timer.schedule(listRefresher, 0, REFRESH_RATE);

    }

    @Override
    public void close() {
        usersList.clear();
        totalUsers.set(0);
        if (listRefresher != null) {
            listRefresher.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @FunctionalInterface
    public interface HttpStatusUpdate {
        void updateHttpLine(String line);
    }

    public static class UserListRefresher extends TimerTask {
        private final BooleanProperty autoUpdate;
        private final Consumer<String> httpStatusConsumer;
        private final Consumer<List<UserDashboard>> usersListUpdater;
        private static final Gson GSON = new Gson();

        public UserListRefresher(BooleanProperty autoUpdate,
                                 Consumer<String> httpStatusConsumer,
                                 Consumer<List<UserDashboard>> usersListUpdater) {
            this.autoUpdate = autoUpdate;
            this.httpStatusConsumer = httpStatusConsumer;
            this.usersListUpdater = usersListUpdater;
        }

        @Override
        public void run() {
            if (!autoUpdate.get()) {

                return;
            }

            final String url = WebConstants.USERS_URL;



            try {
                HttpUtils.getAsync(url).thenAccept(json -> {


                    try {
                        if (json == null || json.trim().isEmpty()) {

                            httpStatusConsumer.accept("No users data received.");
                            usersListUpdater.accept(Collections.emptyList());
                            return;
                        }


                        Type listType = new TypeToken<List<UserDashboard>>() {
                        }.getType();
                        List<UserDashboard> usersList = GSON.fromJson(json, listType);


                        usersListUpdater.accept(usersList);


                    } catch (Exception e) {

                        e.printStackTrace();
                        httpStatusConsumer.accept("Failed to parse users list: " + e.getMessage());
                        usersListUpdater.accept(Collections.emptyList());
                    }
                }).exceptionally(ex -> {

                    if (ex.getCause() != null) {
                        System.err.println("[UserListRefresher] Cause: " + ex.getCause().getMessage());
                    }

                    ex.printStackTrace();
                    httpStatusConsumer.accept("Failed to update users: " + ex.getMessage());
                    usersListUpdater.accept(Collections.emptyList());
                    return null;
                });
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}