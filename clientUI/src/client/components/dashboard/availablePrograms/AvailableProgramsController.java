package client.components.dashboard.availablePrograms;

import client.components.dashboard.availableUsers.AvailableUsersController;
import client.util.HttpUtils;
import emulator.utils.WebConstants;
import execute.dto.ProgramMetadataDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


import static client.util.Constants.REFRESH_RATE;

public class AvailableProgramsController {

    @FXML private TableView<ProgramTableData> availableProgramsTable;
    @FXML private TableColumn<ProgramTableData, String> columnName;
    @FXML private TableColumn<ProgramTableData, String> columnUser;
    @FXML private TableColumn<ProgramTableData, Integer> columnNumberOfInst;
    @FXML private TableColumn<ProgramTableData, Integer> columnMaxDegree;
    @FXML private TableColumn<ProgramTableData, Integer> columnNumberOfRuns;
    @FXML private TableColumn<ProgramTableData, String> columnAverageCreditCost;
    @FXML private Button executuProgramButton;

    private static final Gson GSON = new Gson();
    private final OkHttpClient client = new OkHttpClient();
    private Timer timer;
    private TimerTask listRefresher;
    private BooleanProperty autoUpdate;
    private IntegerProperty totalPrograms;
    private AvailableUsersController.HttpStatusUpdate httpStatusUpdate;


    private ObservableList<ProgramTableData> programsList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        setupTableColumns();
        availableProgramsTable.setItems(programsList);
        autoUpdate = new SimpleBooleanProperty(true);
        totalPrograms = new SimpleIntegerProperty(0);


        executuProgramButton.setOnAction(event -> onExecuteProgramClicked());
    }

    private void setupTableColumns() {
        columnName.setCellValueFactory(data -> data.getValue().nameProperty());
        columnUser.setCellValueFactory(data -> data.getValue().userProperty());
        columnNumberOfInst.setCellValueFactory(data -> data.getValue().numberOfInstProperty().asObject());
        columnMaxDegree.setCellValueFactory(data -> data.getValue().maxDegreeProperty().asObject());
        columnNumberOfRuns.setCellValueFactory(data -> data.getValue().numberOfRunsProperty().asObject());
        columnAverageCreditCost.setCellValueFactory(data -> data.getValue().averageCreditCostProperty());
    }

    private void updateProgramsTable(List<ProgramMetadataDTO> programs) {
        Platform.runLater(() -> {
            programsList.clear();

            for (ProgramMetadataDTO dto : programs) {
                ProgramTableData tableData = new ProgramTableData(
                        dto.getName(),
                        dto.getUploadedBy() != null ? dto.getUploadedBy() : "Unknown",
                        dto.getNumberOfInstructions(),
                        dto.getMaxDegree(),
                        dto.getRunCount(),
                        dto.getAverageCost()
                );
                programsList.add(tableData);
            }
        });
    }

    public void startListRefresher() {
        listRefresher = new ProgramsListRefresher(
                autoUpdate,
                httpStatusUpdate != null ? httpStatusUpdate::updateHttpLine : s -> {},
                this::updateProgramsTable
        );
        timer = new Timer();
        timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
    }

    public void stopListRefresher() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void onExecuteProgramClicked() {
        ProgramTableData selected = availableProgramsTable.getSelectionModel().getSelectedItem();
        //to implement execute button

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // table data binding
    public static class ProgramTableData {
        private final SimpleStringProperty name;
        private final SimpleStringProperty user;
        private final SimpleIntegerProperty numberOfInst;
        private final SimpleIntegerProperty maxDegree;
        private final SimpleIntegerProperty numberOfRuns;
        private final SimpleStringProperty averageCreditCost;

        public ProgramTableData(String name, String user, int numberOfInst,
                                int maxDegree, int numberOfRuns, double avgCost) {
            this.name = new SimpleStringProperty(name);
            this.user = new SimpleStringProperty(user);
            this.numberOfInst = new SimpleIntegerProperty(numberOfInst);
            this.maxDegree = new SimpleIntegerProperty(maxDegree);
            this.numberOfRuns = new SimpleIntegerProperty(numberOfRuns);
            this.averageCreditCost = new SimpleStringProperty(
                    String.format("%.2f", avgCost)
            );
        }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getUser() { return user.get(); }
        public SimpleStringProperty userProperty() { return user; }

        public int getNumberOfInst() { return numberOfInst.get(); }
        public SimpleIntegerProperty numberOfInstProperty() { return numberOfInst; }

        public int getMaxDegree() { return maxDegree.get(); }
        public SimpleIntegerProperty maxDegreeProperty() { return maxDegree; }

        public int getNumberOfRuns() { return numberOfRuns.get(); }
        public SimpleIntegerProperty numberOfRunsProperty() { return numberOfRuns; }

        public String getAverageCreditCost() { return averageCreditCost.get(); }
        public SimpleStringProperty averageCreditCostProperty() { return averageCreditCost; }
    }

    public static class ProgramsListRefresher extends TimerTask {
        private final BooleanProperty autoUpdate;
        private final Consumer<String> httpStatusConsumer;
        private final Consumer<List<ProgramMetadataDTO>> programsListUpdater;
        private static final Gson GSON = new Gson();

        public ProgramsListRefresher(BooleanProperty autoUpdate,
                                     Consumer<String> httpStatusConsumer,
                                     Consumer<List<ProgramMetadataDTO>> programsListUpdater) {
            this.autoUpdate = autoUpdate;
            this.httpStatusConsumer = httpStatusConsumer;
            this.programsListUpdater = programsListUpdater;
        }

        @Override
        public void run() {
            if (autoUpdate.get()) {
                httpStatusConsumer.accept("Updating programs...");

                HttpUtils.getAsync(WebConstants.PROGRAMS_METADATA_URL).thenAccept(json -> {
                    // Parse JSON array of programMetadataDTO
                    try {
                        Type listType = new TypeToken<List<ProgramMetadataDTO>>() {}.getType();
                        List<ProgramMetadataDTO> programs = GSON.fromJson(json, listType);

                        programsListUpdater.accept(programs);
                        httpStatusConsumer.accept("Programs updated.");

                    } catch (Exception e) {
                        httpStatusConsumer.accept("Failed to parse programs list: " + e.getMessage());
                        return;
                    }
                }).exceptionally(ex -> {
                    httpStatusConsumer.accept("Failed to update programs: " + ex.getCause().getMessage());
                    return null;
                });
            }
        }
    }


}
