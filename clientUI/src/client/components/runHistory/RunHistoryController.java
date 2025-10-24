package client.components.runHistory;

import client.components.executionStage.executionStageController;
import client.components.runMenu.RunMenuController;
import client.util.ColumnResizer;
import execute.dto.HistoryDTO;
import execute.dto.VariableDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class RunHistoryController {
    @FXML
    private executionStageController mainController;

    @FXML
    private TableView<HistoryDTO> runHistory;
    @FXML
    public TableColumn<HistoryDTO, Integer> columnNum;
    @FXML
    public TableColumn<HistoryDTO, String> columnDegree;
    @FXML
    public TableColumn<HistoryDTO, String> columnInputs;
    @FXML
    public TableColumn<HistoryDTO, String> columnOutput;
    @FXML
    public TableColumn<HistoryDTO, Integer> columnCycles;
    @FXML
    public TableColumn<HistoryDTO, String> columnProgramName;
    @FXML
    private Button buttonShowStatus;
    @FXML
    private Button buttonReRun;

    private ListProperty<HistoryDTO> historyList;

    @FXML
    public void initialize() {
        this.historyList = new SimpleListProperty<>(FXCollections.observableArrayList());
        runHistory.itemsProperty().bind(historyList);
        runHistory.getColumns().setAll(columnNum, columnProgramName, columnDegree, columnInputs, columnCycles, columnOutput);

        buttonShowStatus.setOnAction(e -> showSelectedRunStatus());
        buttonReRun.setOnAction(e -> rerunSelectedRun());

        // Disable buttons when no selection
        buttonShowStatus.disableProperty().bind(
                runHistory.getSelectionModel().selectedItemProperty().isNull()
        );
        buttonReRun.disableProperty().bind(
                runHistory.getSelectionModel().selectedItemProperty().isNull()
        );

        runHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fitColumns();
        runHistory.widthProperty().addListener((o, a, b) -> fitColumns());
        runHistory.getItems().addListener(
                (javafx.collections.ListChangeListener<? super HistoryDTO>) c ->
                {
                    javafx.application.Platform.runLater(this::fitColumns);
                }
        );

        setupColumns();
    }

    private void setupColumns() {
        setupColumnNum();
        setupColumnProgramName();
        setupColumnDegree();
        setupColumnInputs();
        setupColumnOutput();
        setupColumnCycles();
    }

    private void setupColumnProgramName() {
        columnProgramName.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getProgram().getProgramName()));

        columnProgramName.setCellFactory(col -> new TableCell<HistoryDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    tooltip.setText("Program: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER_LEFT);
            }
        });
    }

    private void setupColumnCycles() {
        columnCycles.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCycles()));

        columnCycles.setCellFactory(col -> new TableCell<HistoryDTO, Integer>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText("(" + item.toString() + ")");
                    setStyle("-fx-text-fill: #df6565;");
                    tooltip.setText("Cycles: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnOutput() {
        columnOutput.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getOutput().toString()));

        columnOutput.setCellFactory(col -> new TableCell<HistoryDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    tooltip.setText("Outputs: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER_LEFT);
            }
        });
    }

    private void setupColumnInputs() {
        columnInputs.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getInputs().toString()));

        columnInputs.setCellFactory(col -> new TableCell<HistoryDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    tooltip.setText("Inputs: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnDegree() {
        columnDegree.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(String.format("%d/%d", cd.getValue().getDegree(), cd.getValue().getMaxDegree())));

        columnDegree.setCellFactory(col -> new TableCell<HistoryDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                    tooltip.setText("Expansion degree: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnNum() {
        columnNum.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNum()));

        columnNum.setCellFactory(col -> new TableCell<HistoryDTO, Integer>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                    tooltip.setText("Num: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    setTooltip(tooltip);

                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void fitColumns() {
        ColumnResizer.lockToContent(runHistory, 2);
    }

    public void setMainController(executionStageController mainController) {
        this.mainController = mainController;

        this.mainController.programSwitchedProperty().addListener((obs, was, now) -> {
            historyList.clear();
        });
    }

    public void addRunHistory(HistoryDTO result) {
        historyList.add(result);
    }

    private HistoryDTO getSelectedRun() {
        return runHistory.getSelectionModel().getSelectedItem();
    }


    private void showSelectedRunStatus() {
        HistoryDTO selected = getSelectedRun();
        if (selected == null) return;

        List<VariableDTO> outputs = selected.getOutputAndTemps();

        // Build a simple string of variable name = value
        StringBuilder sb = new StringBuilder();
        for (VariableDTO var : outputs) {
            sb.append(var.getName()).append(" = ").append(var.getValue()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Run Status");
        alert.setHeaderText("Program variables at the end of run #" + selected.getNum() + ":");
        alert.setContentText(sb.toString());
        Scene scene = runHistory.getScene();

        // Use main window as owner if available
        if (scene != null && scene.getWindow() != null) {
            alert.initOwner(scene.getWindow());
            // Inherit current theme
            alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        // Use app icon on the dialog window (if available)
        try {
            Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
            dlg.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
        } catch (Exception ignored) {  }

        alert.showAndWait();
    }

    private void rerunSelectedRun() {
        HistoryDTO selected = getSelectedRun();
        RunMenuController runMenuController = mainController.getRunMenuController();

        if (selected == null || mainController == null) return;

        // Step 1: Reset inputs to zero (like New Run)
        List<VariableDTO> zeroedInputs = selected.getInputs().stream()
                .map(v -> new VariableDTO(v.getName(), 0L))
                .toList();

        runMenuController.setInputVariables(zeroedInputs);
        runMenuController.refreshInputTable();
        runMenuController.setPreparingNewRun(true);

        // Step 2: Fill inputs with actual values from the selected row
        List<VariableDTO> inputsFromRow = selected.getInputs();

        List<VariableDTO> updatedInputs = zeroedInputs.stream()
                .map(v -> inputsFromRow.stream()
                        .filter(h -> h.getName().equals(v.getName()))
                        .findFirst()
                        .orElse(v))
                .toList();

        runMenuController.setInputVariables(updatedInputs);
        runMenuController.refreshInputTable();

        // Optional: log info
        runMenuController.clearLog();
        runMenuController.log("=== Re-Run Prepared ===");
        runMenuController.log("Program: " + selected.getProgram().getProgramName() +
                " (Degree: " + selected.getDegree() + ")");
        runMenuController.log("Inputs loaded from Run #" + selected.getNum());
        for (VariableDTO input : updatedInputs) {
            runMenuController.log("  " + input.getName() + " = " + input.getValue());
        }
    }
}