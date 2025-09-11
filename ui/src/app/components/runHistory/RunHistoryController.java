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

        runHistory.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        runHistory.getItems().addListener((ListChangeListener<Object>) c -> {
            autoResizeColumns(runHistory);
        });

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
                    tooltip.setText("Inputs: " + item);
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);
                }
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
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void autoResizeColumns(TableView<?> table) {
        for (TableColumn<?, ?> column : table.getColumns()) {
            Text t = new Text(column.getText()); // start with header text
            double max = t.getLayoutBounds().getWidth();

            for (int i = 0; i < table.getItems().size(); i++) {
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }

            column.setPrefWidth(max + 2);
        }
    }

    public void setMainController(AppController mainController) { this.mainController =  mainController; }


}
