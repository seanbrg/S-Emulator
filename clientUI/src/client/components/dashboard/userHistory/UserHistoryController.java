package client.components.dashboard.userHistory;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static client.util.Constants.REFRESH_RATE;

public class UserHistoryController {

    @FXML private TableView<UserHistoryTableData> userHistoryTable;
    @FXML private TableColumn<UserHistoryTableData, Integer> columnRunNumber;
    @FXML private TableColumn<UserHistoryTableData, String> columnType;
    @FXML private TableColumn<UserHistoryTableData, String> columnProgramName;
    @FXML private TableColumn<UserHistoryTableData, String> columnArchitecture;
    @FXML private TableColumn<UserHistoryTableData, Integer> columnRunLevel;
    @FXML private TableColumn<UserHistoryTableData, Long> columnOutputValue;
    @FXML private TableColumn<UserHistoryTableData, Integer> columnCycles;

    private Timer timer;
    private TimerTask refreshTask;
    private ObservableList<UserHistoryTableData> historyList = FXCollections.observableArrayList();
    private StringProperty targetUsername;
    private String currentLoggedInUser;
    private static final Gson GSON = new Gson();
    private static final String USER_HISTORY_URL = "http://localhost:8080/semulator/userhistory";

    @FXML
    public void initialize() {
        setupTableColumns();
        userHistoryTable.setItems(historyList);
        targetUsername = new SimpleStringProperty("");

        targetUsername.addListener((obs, oldVal, newVal) -> {

            String userToShow = (newVal == null || newVal.trim().isEmpty())
                    ? currentLoggedInUser
                    : newVal;

            loadUserHistory(userToShow);
        });

    }
    public void bindToSelectedUsername(StringProperty selectedUsernameProperty) {
        if (selectedUsernameProperty != null) {
            targetUsername.bind(selectedUsernameProperty);
        }
    }


    private void setupTableColumns() {
        columnRunNumber.setCellValueFactory(data -> data.getValue().runNumberProperty().asObject());
        columnType.setCellValueFactory(data -> data.getValue().typeProperty());
        columnProgramName.setCellValueFactory(data -> data.getValue().programNameProperty());
        columnArchitecture.setCellValueFactory(data -> data.getValue().architectureTypeProperty());
        columnRunLevel.setCellValueFactory(data -> data.getValue().runLevelProperty().asObject());
        columnOutputValue.setCellValueFactory(data -> data.getValue().outputValueProperty().asObject());
        columnCycles.setCellValueFactory(data -> data.getValue().cyclesProperty().asObject());
    }

    public void setCurrentUser(String username) {
        this.currentLoggedInUser = username;
        // Initially show current user's history
        if (targetUsername.get() == null || targetUsername.get().isEmpty()) {
            loadUserHistory(username);
        }
    }



    /**
     * Manual method to show history for a specific user
     */
    public void showUserHistory(String username) {
        if (targetUsername.isBound()) {
            targetUsername.unbind();
        }
        targetUsername.set(username);
    }

    /**
     * Show current logged-in user's history
     */
    public void showCurrentUserHistory() {
        if (targetUsername.isBound()) {
            targetUsername.unbind();
        }
        targetUsername.set(currentLoggedInUser);
    }

    private void loadUserHistory(String username) {
        new Thread(() -> {
            try (Response response = HttpUtils.getSync(USER_HISTORY_URL + "?username=" + username)) {

                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    List<UserRunHistory> history = GSON.fromJson(json, new TypeToken<List<UserRunHistory>>(){}.getType());

                    Platform.runLater(() -> {

                        historyList.clear();
                        for (UserRunHistory entry : history) {
                            historyList.add(new UserHistoryTableData(
                                    entry.getRunNumber(),
                                    entry.isMainProgram() ? "Program" : "Function",
                                    entry.getProgramName(),
                                    entry.getArchitectureType(),
                                    entry.getRunLevel(),
                                    entry.getOutputValue(),
                                    entry.getCycles()
                            ));
                        }
                    });
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void updateHistoryTable(List<UserRunHistory> history) {
        Platform.runLater(() -> {
            List<UserHistoryTableData> tableData = new ArrayList<>();

            for (UserRunHistory entry : history) {
                tableData.add(new UserHistoryTableData(
                        entry.getRunNumber(),
                        entry.isMainProgram() ? "Main Program" : "Aux Function",
                        entry.getProgramName(),
                        entry.getArchitectureType(),
                        entry.getRunLevel(),
                        entry.getOutputValue(),
                        entry.getCycles()
                ));
            }

            historyList.setAll(tableData);
        });
    }

    public void startRefresher() {
        if (timer == null) {
            refreshTask = new TimerTask() {
                @Override
                public void run() {
                    String username = targetUsername.get();
                    if (username == null || username.isEmpty()) {
                        username = currentLoggedInUser;
                    }
                    if (username != null && !username.isEmpty()) {
                        loadUserHistory(username);
                    }
                }
            };
            timer = new Timer(true);
            timer.schedule(refreshTask, REFRESH_RATE, REFRESH_RATE);
        }
    }

    public void stopRefresher() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    // Table data class
    public static class UserHistoryTableData {
        private final SimpleIntegerProperty runNumber;
        private final SimpleStringProperty type;
        private final SimpleStringProperty programName;
        private final SimpleStringProperty architectureType;
        private final SimpleIntegerProperty runLevel;
        private final SimpleLongProperty outputValue;
        private final SimpleIntegerProperty cycles;

        public UserHistoryTableData(int runNumber, String type, String programName,
                                    String architectureType, int runLevel,
                                    long outputValue, int cycles) {
            this.runNumber = new SimpleIntegerProperty(runNumber);
            this.type = new SimpleStringProperty(type);
            this.programName = new SimpleStringProperty(programName);
            this.architectureType = new SimpleStringProperty(architectureType);
            this.runLevel = new SimpleIntegerProperty(runLevel);
            this.outputValue = new SimpleLongProperty(outputValue);
            this.cycles = new SimpleIntegerProperty(cycles);
        }

        public SimpleIntegerProperty runNumberProperty() { return runNumber; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty programNameProperty() { return programName; }
        public SimpleStringProperty architectureTypeProperty() { return architectureType; }
        public SimpleIntegerProperty runLevelProperty() { return runLevel; }
        public SimpleLongProperty outputValueProperty() { return outputValue; }
        public SimpleIntegerProperty cyclesProperty() { return cycles; }
    }
}