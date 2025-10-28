package client.components.dashboard.availableUsers;

import client.util.HttpUtils;
import client.util.refresh.UserListRefresher;
import client.util.refresh.UserListRefresher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import users.UserDashboard;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static client.util.Constants.REFRESH_RATE;

public class AvailableUsersController {

    @FXML private TableView<UserTableData> availableUsersTable;
    @FXML private TableColumn<UserTableData, String> columnUsername;
    @FXML private TableColumn<UserTableData, Integer> columnMainPrograms;
    @FXML private TableColumn<UserTableData, Integer> columnSubfunctions;
    @FXML private TableColumn<UserTableData, Integer> columnCurrentCredits;
    @FXML private TableColumn<UserTableData, Integer> columnCreditsUsed;
    @FXML private TableColumn<UserTableData, Integer> columnNumberOfRuns;

    @FXML private TextField creditsField;
    @FXML private Button addCreditsButton;
    @FXML private Button unselectButton;

    private Timer timer;
    private TimerTask listRefresher;
    private BooleanProperty autoUpdate;
    private IntegerProperty totalUsers;
    private StringProperty selectedUsernameProperty;
    private HttpStatusUpdate httpStatusUpdate;

    private ObservableList<UserTableData> usersList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;
    private static final Gson GSON = new Gson();

    public interface HttpStatusUpdate {
        void updateHttpLine(String line);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        availableUsersTable.setItems(usersList);
        autoUpdate = new SimpleBooleanProperty(true);
        totalUsers = new SimpleIntegerProperty(0);
        selectedUsernameProperty = new SimpleStringProperty("");

        Platform.runLater(() -> {
            availableUsersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedUsernameProperty.set(newSelection.getUsername());

                } else {
                    selectedUsernameProperty.set("");

                }
            });
        });

        if (addCreditsButton != null) {
            addCreditsButton.disableProperty().bind(
                    availableUsersTable.getSelectionModel().selectedItemProperty().isNull()
            );
        }

        // Set up unselect button
        if (unselectButton != null) {
            unselectButton.disableProperty().bind(
                    availableUsersTable.getSelectionModel().selectedItemProperty().isNull()
            );
            unselectButton.setOnAction(event -> handleUnselectUser());
        }

        // Set up add credits button
        if (addCreditsButton != null) {
            addCreditsButton.setOnAction(event -> handleAddCredits());
        }
    }

    private void setupTableColumns() {
        columnUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        columnMainPrograms.setCellValueFactory(data -> data.getValue().mainProgramsProperty().asObject());
        columnSubfunctions.setCellValueFactory(data -> data.getValue().subfunctionsProperty().asObject());
        columnCurrentCredits.setCellValueFactory(data -> data.getValue().currentCreditsProperty().asObject());
        columnCreditsUsed.setCellValueFactory(data -> data.getValue().creditsUsedProperty().asObject());
        columnNumberOfRuns.setCellValueFactory(data -> data.getValue().numberOfRunsProperty().asObject());
    }

    public void updateUsersTable(List<UserDashboard> users) {
        Platform.runLater(() -> {
            String selectedUsername = null;
            UserTableData current = availableUsersTable.getSelectionModel().getSelectedItem();
            if (current != null) {
                selectedUsername = current.getUsername();
            }

            List<UserTableData> newTableData = new ArrayList<>();
            for (UserDashboard user : users) {
                UserTableData tableData = new UserTableData(
                        user.getUsername(),
                        user.getMainProgramsUploaded(),
                        user.getSubfunctionsContributed(),
                        user.getCurrentCredits(),
                        user.getCreditsUsed(),
                        user.getNumberOfRuns()
                );
                newTableData.add(tableData);
            }
            usersList.setAll(newTableData);

            // Restore selection by matching username
            if (selectedUsername != null) {
                for (UserTableData u : usersList) {
                    if (selectedUsername.equals(u.getUsername())) {
                        availableUsersTable.getSelectionModel().select(u);
                        break;
                    }
                }
            }

            totalUsers.set(users.size());
        });
    }

    public void startListRefresher() {
        listRefresher = new UserListRefresher(
                autoUpdate,
                httpStatusUpdate != null ? httpStatusUpdate::updateHttpLine : s -> {},
                this::updateUsersTable
        );
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public void close() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (listRefresher != null) {
            listRefresher.cancel();
            listRefresher = null;
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void setHttpStatusUpdate(HttpStatusUpdate httpStatusUpdate) {
        this.httpStatusUpdate = httpStatusUpdate;
    }

    public IntegerProperty totalUsersProperty() {return totalUsers;}

    public StringProperty selectedUsernameProperty() {return selectedUsernameProperty;}

    private void handleUnselectUser() {availableUsersTable.getSelectionModel().clearSelection();}

    private void handleAddCredits() {
        UserTableData selected = availableUsersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No User Selected", "Please select a user to add credits.");
            return;
        }

        String creditsText = creditsField.getText();
        if (creditsText == null || creditsText.trim().isEmpty()) {
            showAlert("Invalid Input", "Please enter a credit amount.");
            return;
        }

        try {
            int credits = Integer.parseInt(creditsText.trim());
            if (credits <= 0) {
                showAlert("Invalid Input", "Credits must be a positive number.");
                return;
            }

            // Send request to server
            addCreditsToUser(selected.getUsername(), credits);

            // Clear field after successful submission
            creditsField.clear();

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number.");
        }
    }

    private void addCreditsToUser(String username, int credits) {
        new Thread(() -> {
            try {
                String url = "http://localhost:8080/semulator/users";
                String formData = "username=" + username + "&credits=" + credits;
                RequestBody body = RequestBody.create(
                        formData,
                        MediaType.parse("application/x-www-form-urlencoded")
                );

                try (Response response = HttpUtils.postSync(url, body)) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            showAlert("Success", "Added " + credits + " credits to " + username);
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert("Error", "Failed to add credits: " + response.message());
                        });
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to add credits: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Table data binding class
    public static class UserTableData {
        private final SimpleStringProperty username;
        private final SimpleIntegerProperty mainPrograms;
        private final SimpleIntegerProperty subfunctions;
        private final SimpleIntegerProperty currentCredits;
        private final SimpleIntegerProperty creditsUsed;
        private final SimpleIntegerProperty numberOfRuns;

        public UserTableData(String username, int mainPrograms, int subfunctions,
                             int currentCredits, int creditsUsed, int numberOfRuns) {
            this.username = new SimpleStringProperty(username);
            this.mainPrograms = new SimpleIntegerProperty(mainPrograms);
            this.subfunctions = new SimpleIntegerProperty(subfunctions);
            this.currentCredits = new SimpleIntegerProperty(currentCredits);
            this.creditsUsed = new SimpleIntegerProperty(creditsUsed);
            this.numberOfRuns = new SimpleIntegerProperty(numberOfRuns);
        }

        public String getUsername() { return username.get(); }
        public SimpleStringProperty usernameProperty() { return username; }

        public int getMainPrograms() { return mainPrograms.get(); }
        public SimpleIntegerProperty mainProgramsProperty() { return mainPrograms; }

        public int getSubfunctions() { return subfunctions.get(); }
        public SimpleIntegerProperty subfunctionsProperty() { return subfunctions; }

        public int getCurrentCredits() { return currentCredits.get(); }
        public SimpleIntegerProperty currentCreditsProperty() { return currentCredits; }

        public int getCreditsUsed() { return creditsUsed.get(); }
        public SimpleIntegerProperty creditsUsedProperty() { return creditsUsed; }

        public int getNumberOfRuns() { return numberOfRuns.get(); }
        public SimpleIntegerProperty numberOfRunsProperty() { return numberOfRuns; }
    }
}