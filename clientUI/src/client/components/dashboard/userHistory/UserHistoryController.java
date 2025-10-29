package client.components.dashboard.userHistory;

import client.components.dashboard.availablePrograms.AvailableProgramsController;
import client.components.dashboard.availableUsers.AvailableUsersController;
import client.util.HttpUtils;
import client.util.refresh.HistoriesListRefresher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.HistoryDTO;
import execute.dto.ProgramMetadataDTO;
import execute.dto.UserRunHistoryDTO;
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
import users.UserDashboard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static client.util.Constants.REFRESH_RATE;

public class UserHistoryController {

    // Table and columns
    @FXML private TableView<UserRunHistoryDTO> userHistoryTable;
    @FXML private TableColumn<UserRunHistoryDTO, Number> columnRunNumber;
    @FXML private TableColumn<UserRunHistoryDTO, String> columnType;
    @FXML private TableColumn<UserRunHistoryDTO, String> columnProgramName;
    @FXML private TableColumn<UserRunHistoryDTO, String> columnArchitecture;
    @FXML private TableColumn<UserRunHistoryDTO, Number> columnRunLevel;
    @FXML private TableColumn<UserRunHistoryDTO, Number> columnOutputValue;
    @FXML private TableColumn<UserRunHistoryDTO, Number> columnCycles;

    // Buttons
    @FXML private Button showStatusButton;
    @FXML private Button rerunButton;

    private final ObservableList<UserRunHistoryDTO> historyList = FXCollections.observableArrayList();
    private final Gson GSON = new Gson();
    private StringProperty selectedUsername;
    private Timer timer;
    private TimerTask listRefresher;
    private BooleanProperty autoUpdate;
    private HttpStatusUpdate httpStatusUpdate;


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

        selectedUsername = new SimpleStringProperty();
        autoUpdate = new SimpleBooleanProperty(true);


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

    public interface HttpStatusUpdate {
        void updateHttpLine(String line);
    }

    public StringProperty selectedUsernameProperty() {
        return selectedUsername;
    }

    private void updateUsersTable(List<UserRunHistoryDTO> histories) {
        Platform.runLater(() -> {
            UserRunHistoryDTO current = userHistoryTable.getSelectionModel().getSelectedItem();
            historyList.setAll(histories);

            // restore selection by matching name
            if (current != null) {
                for (UserRunHistoryDTO p : historyList) {
                    if (current.toString().equals(p.toString())) {
                        userHistoryTable.getSelectionModel().select(p);
                        break;
                    }
                }
            }
        });
    }

    // Buttons handlers (optional, implement as needed)
    @FXML
    private void onShowStatus() {
        UserRunHistoryDTO selected = userHistoryTable.getSelectionModel().getSelectedItem();
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

    public void startListRefresher() {
        listRefresher = new HistoriesListRefresher(
                selectedUsername,
                autoUpdate,
                httpStatusUpdate != null ? httpStatusUpdate::updateHttpLine : s -> {},
                this::updateUsersTable
        );
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    // stop refresher
    public void stopRefresher() {
        if (refresherTimer != null) {
            refresherTimer.cancel();
            refresherTimer = null;
        }
    }

}
