package app.components.runHistory;

import app.components.body.AppController;
import app.components.programTab.ProgramTabController;
import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class RunHistoryController {
    @FXML private AppController mainController;

    @FXML private TableView<HistoryDTO> runHistory;
    @FXML public TableColumn<HistoryDTO, Integer> columnNum;
    @FXML public TableColumn<HistoryDTO, Integer> columnDegree;
    @FXML public TableColumn<HistoryDTO, String> columnInputs;
    @FXML public TableColumn<HistoryDTO, String> columnOutput;
    @FXML public TableColumn<HistoryDTO, Integer> columnCycles;

    private ListProperty<HistoryDTO> historyList;

    @FXML
    public void initialize() {
        this.historyList = new SimpleListProperty<>(FXCollections.observableArrayList());
        runHistory.itemsProperty().bind(historyList);

        runHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        runHistory.getColumns().setAll(columnNum, columnDegree, columnInputs, columnCycles, columnOutput);

        fitColumns();
        runHistory.widthProperty().addListener((o, a, b) -> fitColumns());
        runHistory.getItems().addListener(
                (javafx.collections.ListChangeListener<? super HistoryDTO>) c ->
                        javafx.application.Platform.runLater(this::fitColumns)
        );


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
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnOutput() {
        columnOutput.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getOutput().getVarString()));

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
                    tooltip.setText("Output: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
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
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnDegree() {
        columnDegree.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getDegree()));

        columnDegree.setCellFactory(col -> new TableCell<HistoryDTO, Integer>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                    tooltip.setText("Expansion degree: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(500));
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
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);

                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void fitColumns() {
        lockToContent(columnNum,     1);
        lockToContent(columnDegree,  1);
        lockToContent(columnCycles,  1);
        lockToContent(columnInputs,  70);

        // columnOutput flexes under CONSTRAINED policy
        columnOutput.setResizable(true);
        columnOutput.setMinWidth(0);
        columnOutput.setPrefWidth(1);
        columnOutput.setMaxWidth(Double.MAX_VALUE);
    }

    private void lockToContent(TableColumn<?, ?> col, double pad) {
        double w = headerAndCellsWidth(col) + pad;
        col.setResizable(false);
        col.setMinWidth(w);
        col.setPrefWidth(w);
        col.setMaxWidth(w);
    }

    private double headerAndCellsWidth(TableColumn<?, ?> col) {
        double max = textW(col.getText());
        for (int i = 0; i < runHistory.getItems().size(); i++) {
            Object v = col.getCellData(i);
            if (v != null) max = Math.max(max, textW(String.valueOf(v)));
        }
        return Math.ceil(max);
    }

    private double textW(String s) {
        return new javafx.scene.text.Text(s == null ? "" : s).getLayoutBounds().getWidth();
    }


    public void setMainController(AppController mainController) { this.mainController =  mainController; }


    public void addRunHistory(HistoryDTO result) {
        historyList.add(result);
    }
}
