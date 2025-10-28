package client.components.dashboard.userHistory;

import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import execute.dto.HistoryDTO;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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
    @FXML private Button showStatusButton;
    @FXML private Button rerunButton;

    private Timer timer;
    private TimerTask refreshTask;
    private ObservableList<UserHistoryTableData> historyList = FXCollections.observableArrayList();
    private StringProperty targetUsername;
    private String currentLoggedInUser;
    private static final Gson GSON = new Gson();
    private static final String USER_HISTORY_URL = "http://localhost:8080/semulator/userhistory";
    private static final String RUN_DETAILS_URL = "http://localhost:8080/semulator/userhistory/run";

    // Reference to main controller for re-run functionality
    private Object mainController;

    @FXML
    public void initialize() {
        setupTableColumns();
        userHistoryTable.setItems(historyList);
        targetUsername = new SimpleStringProperty("");

        // Disable buttons when no selection
        showStatusButton.setDisable(true);
        rerunButton.setDisable(true);

        // Enable buttons when a row is selected
        userHistoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            showStatusButton.setDisable(!hasSelection);
            rerunButton.setDisable(!hasSelection);
        });

        targetUsername.addListener((obs, oldVal, newVal) -> {
            String userToShow = (newVal == null || newVal.trim().isEmpty())
                    ? currentLoggedInUser
                    : newVal;

            loadUserHistory(userToShow);
        });
    }

    public void setMainController(Object mainController) {
        this.mainController = mainController;
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
                        if (history != null) {
                            for (UserRunHistory entry : history) {
                                historyList.add(new UserHistoryTableData(
                                        entry.getRunNumber(),
                                        entry.isMainProgram() ? "Program" : "Function",
                                        entry.getProgramName(),
                                        entry.getArchitectureType(),
                                        entry.getRunLevel(),
                                        entry.getOutputValue(),
                                        entry.getCycles(),
                                        entry // Store the full entry for later use
                                ));
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Error loading user history: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onShowStatus() {
        UserHistoryTableData selected = getSelectedRun();
        if (selected == null) return;

        // Fetch detailed run information from server
        new Thread(() -> {
            try {
                UserRunHistory runHistory = selected.getFullHistoryEntry();
                String url = RUN_DETAILS_URL + "?username=" + runHistory.getUsername() +
                        "&runNumber=" + runHistory.getRunNumber();

                try (Response response = HttpUtils.getSync(url)) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        HistoryDTO historyDTO = GSON.fromJson(json, HistoryDTO.class);

                        Platform.runLater(() -> showStatusDialog(historyDTO, selected.runNumberProperty().get()));
                    } else {
                        Platform.runLater(() -> showError("Failed to fetch run details"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Error fetching run details: " + e.getMessage()));
            }
        }).start();
    }

    private void showStatusDialog(HistoryDTO historyDTO, int runNumber) {
        List<VariableDTO> outputs = historyDTO.getOutputAndTemps();

        StringBuilder sb = new StringBuilder();
        for (VariableDTO var : outputs) {
            sb.append(var.getName()).append(" = ").append(var.getValue()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Run Status");
        alert.setHeaderText("Program variables at the end of run #" + runNumber + ":");
        alert.setContentText(sb.toString());

        Scene scene = userHistoryTable.getScene();
        if (scene != null && scene.getWindow() != null) {
            alert.initOwner(scene.getWindow());
            alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        try {
            Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
            dlg.getIcons().add(new Image(getClass().getResourceAsStream("/app/resources/images/icon.png")));
        } catch (Exception ignored) { /* icon optional */ }

        alert.showAndWait();
    }

    @FXML
    private void onRerun() {
        UserHistoryTableData selected = getSelectedRun();
        if (selected == null) return;

        // Fetch detailed run information to get inputs
        new Thread(() -> {
            try {
                UserRunHistory runHistory = selected.getFullHistoryEntry();
                String url = RUN_DETAILS_URL + "?username=" + runHistory.getUsername() +
                        "&runNumber=" + runHistory.getRunNumber();

                try (Response response = HttpUtils.getSync(url)) {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        HistoryDTO historyDTO = GSON.fromJson(json, HistoryDTO.class);

                        Platform.runLater(() -> rerunWithHistory(historyDTO));
                    } else {
                        Platform.runLater(() -> showError("Failed to fetch run details for re-run"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Error fetching run details: " + e.getMessage()));
            }
        }).start();
    }

    private void rerunWithHistory(HistoryDTO historyDTO) {
        if (mainController == null) {
            showError("Cannot re-run: main controller not set");
            return;
        }

        try {
            // Use reflection to get the RunMenuController
            java.lang.reflect.Method getRunMenuMethod = mainController.getClass().getMethod("getRunMenuController");
            Object runMenuController = getRunMenuMethod.invoke(mainController);

            if (runMenuController == null) {
                showError("Run menu controller not available");
                return;
            }

            // Step 1: Reset inputs to zero
            List<VariableDTO> zeroedInputs = historyDTO.getInputs().stream()
                    .map(v -> new VariableDTO(v.getName(), 0L))
                    .toList();

            java.lang.reflect.Method setInputVariablesMethod = runMenuController.getClass().getMethod("setInputVariables", List.class);
            java.lang.reflect.Method refreshInputTableMethod = runMenuController.getClass().getMethod("refreshInputTable");
            java.lang.reflect.Method setPreparingNewRunMethod = runMenuController.getClass().getMethod("setPreparingNewRun", boolean.class);
            java.lang.reflect.Method clearLogMethod = runMenuController.getClass().getMethod("clearLog");
            java.lang.reflect.Method logMethod = runMenuController.getClass().getMethod("log", String.class);

            setInputVariablesMethod.invoke(runMenuController, zeroedInputs);
            refreshInputTableMethod.invoke(runMenuController);
            setPreparingNewRunMethod.invoke(runMenuController, true);

            // Step 2: Fill inputs with actual values from the selected row
            List<VariableDTO> inputsFromRow = historyDTO.getInputs();
            List<VariableDTO> updatedInputs = zeroedInputs.stream()
                    .map(v -> inputsFromRow.stream()
                            .filter(h -> h.getName().equals(v.getName()))
                            .findFirst()
                            .orElse(v))
                    .toList();

            setInputVariablesMethod.invoke(runMenuController, updatedInputs);
            refreshInputTableMethod.invoke(runMenuController);

            // Optional: log info
            clearLogMethod.invoke(runMenuController);
            logMethod.invoke(runMenuController, "=== Re-Run Prepared ===");
            logMethod.invoke(runMenuController, "Program: " + historyDTO.getProgram().getProgramName() +
                    " (Degree: " + historyDTO.getDegree() + ")");
            logMethod.invoke(runMenuController, "Inputs loaded from Run #" + historyDTO.getNum());
            for (VariableDTO input : updatedInputs) {
                logMethod.invoke(runMenuController, "  " + input.getName() + " = " + input.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error setting up re-run: " + e.getMessage());
        }
    }

    private UserHistoryTableData getSelectedRun() {
        return userHistoryTable.getSelectionModel().getSelectedItem();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Scene scene = userHistoryTable.getScene();
        if (scene != null && scene.getWindow() != null) {
            alert.initOwner(scene.getWindow());
            alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        alert.showAndWait();
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
        private final UserRunHistory fullHistoryEntry;

        public UserHistoryTableData(int runNumber, String type, String programName,
                                    String architectureType, int runLevel,
                                    long outputValue, int cycles, UserRunHistory fullHistoryEntry) {
            this.runNumber = new SimpleIntegerProperty(runNumber);
            this.type = new SimpleStringProperty(type);
            this.programName = new SimpleStringProperty(programName);
            this.architectureType = new SimpleStringProperty(architectureType);
            this.runLevel = new SimpleIntegerProperty(runLevel);
            this.outputValue = new SimpleLongProperty(outputValue);
            this.cycles = new SimpleIntegerProperty(cycles);
            this.fullHistoryEntry = fullHistoryEntry;
        }

        public SimpleIntegerProperty runNumberProperty() { return runNumber; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty programNameProperty() { return programName; }
        public SimpleStringProperty architectureTypeProperty() { return architectureType; }
        public SimpleIntegerProperty runLevelProperty() { return runLevel; }
        public SimpleLongProperty outputValueProperty() { return outputValue; }
        public SimpleIntegerProperty cyclesProperty() { return cycles; }
        public UserRunHistory getFullHistoryEntry() { return fullHistoryEntry; }
    }
}