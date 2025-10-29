package client.components.dashboard.userHistory;

import client.components.mainApp.MainAppController;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static client.util.Constants.REFRESH_RATE;

public class UserHistoryController {

    // Table and columns
    @FXML private TableView<UserRunHistory> userHistoryTable;
    @FXML private TableColumn<UserRunHistory, Number> columnRunNumber;
    @FXML private TableColumn<UserRunHistory, String> columnType;
    @FXML private TableColumn<UserRunHistory, String> columnProgramName;
    @FXML private TableColumn<UserRunHistory, String> columnArchitecture;
    @FXML private TableColumn<UserRunHistory, Number> columnRunLevel;
    @FXML private TableColumn<UserRunHistory, Number> columnOutputValue;
    @FXML private TableColumn<UserRunHistory, Number> columnCycles;

    // Buttons
    @FXML private Button showStatusButton;
    @FXML private Button rerunButton;

    private final ObservableList<UserRunHistory> historyList = FXCollections.observableArrayList();
    private final Gson GSON = new Gson();
    private String currentUser;

    @FXML
    private void initialize() {
        // Bind columns to UserRunHistory properties
        columnRunNumber.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRunNumber()));
        columnType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isMainProgram() ? "Program" : "Function"));
        columnProgramName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProgramName()));
        columnArchitecture.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArchitectureType()));
        columnRunLevel.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getRunLevel()));
        columnOutputValue.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getOutputValue()));
        columnCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCycles()));

        // Set items
        userHistoryTable.setItems(historyList);

        // Disable buttons until selection
        showStatusButton.setDisable(true);
        rerunButton.setDisable(true);

        // Enable buttons when a row is selected
        userHistoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            showStatusButton.setDisable(!hasSelection);
            rerunButton.setDisable(!hasSelection);
        });
    }

    /** Called by DashboardStageController */
    public void bindToSelectedUsername(StringProperty selectedUsernameProperty) {
        selectedUsernameProperty.addListener((obs, oldVal, newVal) -> {
            String userToShow = (newVal == null || newVal.trim().isEmpty())
                    ? currentUser
                    : newVal;
            loadUserHistory(userToShow);
        });
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
        loadUserHistory(username);
    }

    private void loadUserHistory(String username) {
        if (username == null || username.isBlank()) return;

        String url = "http://localhost:8080/semulator/run?username=" + username;
        HttpUtils.getAsync(url)
                .thenAcceptAsync(json -> {
                    Type listType = new TypeToken<List<HistoryDTO>>() {}.getType();
                    List<HistoryDTO> historyDTOList = GSON.fromJson(json, listType);

                    if (historyDTOList == null) return;

                    List<UserRunHistory> userRunHistories = new ArrayList<>();
                    int runNumber = 1;
                    for (HistoryDTO dto : historyDTOList) {
                        UserRunHistory runHistory = new UserRunHistory();
                        runHistory.setUsername(username);
                        runHistory.setRunNumber(runNumber++);
                        runHistory.setMainProgram(dto.getProgram().isMainProgram());
                        runHistory.setProgramName(dto.getProgram().getProgramName());
                        runHistory.setArchitectureType(dto.getProgram().getArchitectureType());
                        runHistory.setRunLevel(dto.getDegree());
                        runHistory.setOutputValue(dto.getOutput() != null ? dto.getOutput().getValue() : 0);
                        runHistory.setCycles(dto.getCycles());
                        runHistory.setTimestamp(System.currentTimeMillis());
                        runHistory.setHistoryDTO(dto);
                        userRunHistories.add(runHistory);
                    }

                    Platform.runLater(() -> {
                        historyList.clear();
                        historyList.addAll(userRunHistories);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    // Buttons handlers (optional, implement as needed)
    @FXML
    private void onShowStatus() {
        UserRunHistory selected = userHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        HistoryDTO dto = selected.getHistoryDTO();
        if (dto == null) return;

        StringBuilder sb = new StringBuilder();
        for (VariableDTO var : dto.getOutputAndTemps()) {
            sb.append(var.getName()).append(" = ").append(var.getValue()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Run Status");
        alert.setHeaderText("Program variables at the end of run #" + selected.getRunNumber());
        alert.setContentText(sb.toString());

        Scene scene = userHistoryTable.getScene();
        if (scene != null && scene.getWindow() != null) {
            alert.initOwner(scene.getWindow());
            alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        try {
            Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
            dlg.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
        } catch (Exception ignored) {}

        alert.showAndWait();
    }

    @FXML
    private void onRerun() {
        // Implement re-run logic here
    }


    // add these fields
    private Timer refresherTimer;

    // start refresher
    public void startRefresher() {
        stopRefresher(); // make sure only one timer is running
        if (currentUser == null || currentUser.isBlank()) return;

        refresherTimer = new Timer(true); // daemon thread
        refresherTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null && !currentUser.isBlank()) {
                    loadUserHistory(currentUser);
                }
            }
        }, 0, REFRESH_RATE);
    }

    // stop refresher
    public void stopRefresher() {
        if (refresherTimer != null) {
            refresherTimer.cancel();
            refresherTimer = null;
        }
    }

}
