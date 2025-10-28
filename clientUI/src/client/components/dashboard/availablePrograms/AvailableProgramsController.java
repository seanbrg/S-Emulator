package client.components.dashboard.availablePrograms;

import client.components.dashboard.availableFunctions.AvailableFunctionsController;
import client.components.dashboard.availableUsers.AvailableUsersController;
import client.components.dashboard.dashboardStage.DashboardStageController;
import client.util.refresh.ProgramsListRefresher;
import execute.dto.InstructionDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;


import static client.util.Constants.REFRESH_RATE;

public class AvailableProgramsController {

    @FXML private TableView<ProgramTableData> availableProgramsTable;
    @FXML private TableColumn<ProgramTableData, String> columnName;
    @FXML private TableColumn<ProgramTableData, String> columnUser;
    @FXML private TableColumn<ProgramTableData, Integer> columnNumberOfInst;
    @FXML private TableColumn<ProgramTableData, Integer> columnMaxDegree;
    @FXML private TableColumn<ProgramTableData, Integer> columnNumberOfRuns;
    @FXML private TableColumn<ProgramTableData, String> columnAverageCreditCost;
    @FXML private Button executeProgramButton;

    private Timer timer;
    private TimerTask listRefresher;
    private BooleanProperty autoUpdate;
    private IntegerProperty totalPrograms;
    private StringProperty selectedProgramNameProperty;
    private AvailableUsersController.HttpStatusUpdate httpStatusUpdate;
    private DashboardStageController mainDashboardController;


    private ObservableList<ProgramTableData> programsList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        setupTableColumns();
        availableProgramsTable.setItems(programsList);
        autoUpdate = new SimpleBooleanProperty(true);
        totalPrograms = new SimpleIntegerProperty(0);
        selectedProgramNameProperty = new SimpleStringProperty("");
        executeProgramButton.setOnAction(event -> onExecuteProgramClicked());

        Platform.runLater(() -> {
            availableProgramsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedProgramNameProperty.set(newSelection.getName());
                }
            });
        });

        executeProgramButton.disableProperty().bind(
                availableProgramsTable.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    public void setMainDashboardController(DashboardStageController controller) {
        this.mainDashboardController = controller;
    }

    private void setupTableColumns() {
        columnName.setCellValueFactory(data -> data.getValue().nameProperty());
        columnUser.setCellValueFactory(data -> data.getValue().userProperty());
        columnNumberOfInst.setCellValueFactory(data -> data.getValue().numberOfInstProperty().asObject());
        columnMaxDegree.setCellValueFactory(data -> data.getValue().maxDegreeProperty().asObject());
        columnNumberOfRuns.setCellValueFactory(data -> data.getValue().numberOfRunsProperty().asObject());
        columnAverageCreditCost.setCellValueFactory(data -> data.getValue().averageCreditCostProperty());
    }

    public void updateProgramsTable(List<ProgramMetadataDTO> programs) {
        Platform.runLater(() -> {
            // remember selected program name
            String selectedName = null;
            ProgramTableData current = availableProgramsTable.getSelectionModel().getSelectedItem();
            if (current != null) selectedName = current.getName();

            List<ProgramTableData> newTableData = new ArrayList<>();
            for (ProgramMetadataDTO dto : programs) {
                ProgramTableData tableData = new ProgramTableData(
                        dto.getName(),
                        dto.getUploadedBy() != null ? dto.getUploadedBy() : "Unknown",
                        dto.getNumberOfInstructions(),
                        dto.getMaxDegree(),
                        dto.getRunCount(),
                        dto.getAverageCost()
                );
                newTableData.add(tableData);
            }
            programsList.setAll(newTableData);

            // restore selection by matching name
            if (selectedName != null) {
                for (ProgramTableData p : programsList) {
                    if (selectedName.equals(p.getName())) {
                        availableProgramsTable.getSelectionModel().select(p);
                        break;
                    }
                }
            }
        });
    }


    public void startListRefresher() {
        listRefresher = new ProgramsListRefresher(
                autoUpdate,
                s->{}, // no status update
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

    public StringProperty selectedProgramNameProperty() {
        return selectedProgramNameProperty;
    }

    private void onExecuteProgramClicked() {
        ProgramTableData selected = availableProgramsTable.getSelectionModel().getSelectedItem();
        List<String> funcNames = mainDashboardController.getDisplayedFuncNames();
        if (selected != null && mainDashboardController != null) {
            mainDashboardController.switchToExecute(funcNames);
        }
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
}
