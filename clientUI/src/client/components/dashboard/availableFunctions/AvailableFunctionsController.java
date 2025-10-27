package client.components.dashboard.availableFunctions;

import client.util.refresh.FunctionsListRefresher;
import execute.dto.FunctionMetadataDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import static client.util.Constants.REFRESH_RATE;

public class AvailableFunctionsController {

    @FXML public Button executeFunctionButton;
    @FXML public TableView<FunctionTableData> availableFunctionsTable;
    @FXML public TableColumn<FunctionTableData, String> columnFuncName;
    @FXML public TableColumn<FunctionTableData, String> columnProgramName;
    @FXML public TableColumn<FunctionTableData, String> columnUser;
    @FXML public TableColumn<FunctionTableData, Integer> columnNumberOfInst;
    @FXML public TableColumn<FunctionTableData, Integer> columnMaxDegree;

    private Timer timer;
    private FunctionsListRefresher listRefresher;
    private BooleanProperty autoUpdate;
    private IntegerProperty totalFunctions;
    StringProperty selectedProgramNameProperty;
    private ObservableList<FunctionTableData> functionsList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;


    @FXML
    public void initialize() {
        setupTableColumns();
        availableFunctionsTable.setItems(functionsList);
        autoUpdate = new SimpleBooleanProperty(true);
        totalFunctions = new SimpleIntegerProperty(0);
        selectedProgramNameProperty = new SimpleStringProperty("");

        executeFunctionButton.setOnAction(event -> onExecuteFunctionClicked());
    }

    private void setupTableColumns() {
        columnFuncName.setCellValueFactory(data -> data.getValue().nameProperty());
        columnProgramName.setCellValueFactory(data -> data.getValue().programProperty());
        columnUser.setCellValueFactory(data -> data.getValue().userProperty());
        columnNumberOfInst.setCellValueFactory(data -> data.getValue().numberOfInstProperty().asObject());
        columnMaxDegree.setCellValueFactory(data -> data.getValue().maxDegreeProperty().asObject());
    }

    private void updateFunctionsTable(List<FunctionMetadataDTO> functions) {
        Platform.runLater(() -> {
            functionsList.clear();

            for (FunctionMetadataDTO dto : functions) {
                FunctionTableData tableData = new FunctionTableData(
                        dto.getName(),
                        dto.getProgram(),
                        dto.getUploadedBy() != null ? dto.getUploadedBy() : "Unknown",
                        dto.getNumberOfInstructions(),
                        dto.getMaxDegree()
                );
                functionsList.add(tableData);
            }
        });
    }

    public void startListRefresher() {
        Platform.runLater(() -> {
            if (selectedProgramNameProperty != null) {
                listRefresher = new FunctionsListRefresher(
                        selectedProgramNameProperty,
                        autoUpdate,
                        s->{}, // no status update
                        this::updateFunctionsTable
                );
                timer = new Timer();
                timer.schedule(listRefresher, REFRESH_RATE, REFRESH_RATE);
            }
        });
    }

    public void stopListRefresher() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void onExecuteFunctionClicked() {
        FunctionTableData selected = availableFunctionsTable.getSelectionModel().getSelectedItem();
        //TODO: implement execute button for functions

    }

    public StringProperty selectedProgramNameProperty() { return selectedProgramNameProperty; }

    // table data binding
    public static class FunctionTableData {
        private final SimpleStringProperty name;
        private final SimpleStringProperty program;
        private final SimpleStringProperty user;
        private final SimpleIntegerProperty numberOfInst;
        private final SimpleIntegerProperty maxDegree;

        public FunctionTableData(String name, String program, String user,
                                 int numberOfInst, int maxDegree) {
            this.name = new SimpleStringProperty(name);
            this.program = new SimpleStringProperty(program);
            this.user = new SimpleStringProperty(user);
            this.numberOfInst = new SimpleIntegerProperty(numberOfInst);
            this.maxDegree = new SimpleIntegerProperty(maxDegree);
        }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getProgram() { return program.get(); }
        public SimpleStringProperty programProperty() { return program; }

        public String getUser() { return user.get(); }
        public SimpleStringProperty userProperty() { return user; }

        public int getNumberOfInst() { return numberOfInst.get(); }
        public SimpleIntegerProperty numberOfInstProperty() { return numberOfInst; }

        public int getMaxDegree() { return maxDegree.get(); }
        public SimpleIntegerProperty maxDegreeProperty() { return maxDegree; }
    }
}
