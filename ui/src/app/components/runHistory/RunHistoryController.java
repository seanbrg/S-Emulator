package app.components.runHistory;

import app.util.ColumnResizer;
import app.components.body.AppController;
import execute.dto.HistoryDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class RunHistoryController {
    @FXML private AppController mainController;

    @FXML private TableView<HistoryDTO> runHistory;
    @FXML public TableColumn<HistoryDTO, Integer> columnNum;
    @FXML public TableColumn<HistoryDTO, String> columnDegree;
    @FXML public TableColumn<HistoryDTO, String> columnInputs;
    @FXML public TableColumn<HistoryDTO, String> columnOutput;
    @FXML public TableColumn<HistoryDTO, Integer> columnCycles;

    private ListProperty<HistoryDTO> historyList;

    @FXML
    public void initialize() {
        this.historyList = new SimpleListProperty<>(FXCollections.observableArrayList());
        runHistory.itemsProperty().bind(historyList);
        runHistory.getColumns().setAll(columnNum, columnDegree, columnInputs, columnCycles, columnOutput);

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
        setupColumnDegree();
        setupColumnInputs();
        setupColumnOutput();
        setupColumnCycles();
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

    public void setMainController(AppController mainController) {
        this.mainController =  mainController;

        this.mainController.programSwitchedProperty().addListener((obs, was, now) -> {
            historyList.clear();
        });
    }


    public void addRunHistory(HistoryDTO result) {
        historyList.add(result);
    }
}
