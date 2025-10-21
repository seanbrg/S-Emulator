package app.components.instructionHistory;

import app.util.ColumnResizer;
import app.components.body.AppController;
import execute.dto.InstructionDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.List;

public class InstructionHistoryController {
    @FXML private AppController mainController;

    @FXML private TableView<InstructionDTO> instructionHistory;
    @FXML private TableColumn<InstructionDTO, String> columnLabel;
    @FXML private TableColumn<InstructionDTO, String> columnInstruction;
    @FXML private TableColumn<InstructionDTO, String> columnType;
    @FXML private TableColumn<InstructionDTO, Integer> columnNum;
    @FXML private TableColumn<InstructionDTO, Integer> columnCycles;


    private ListProperty<InstructionDTO> instrHistoryList;

    @FXML
    public void initialize() {
        this.instrHistoryList = new SimpleListProperty<>(FXCollections.observableArrayList());
        instructionHistory.itemsProperty().bind(instrHistoryList);

        instructionHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fitColumns();
        instructionHistory.widthProperty().addListener((o, a, b) -> fitColumns());
        instructionHistory.getItems().addListener(
                (javafx.collections.ListChangeListener<? super InstructionDTO>) c ->
                {
                    javafx.application.Platform.runLater(this::fitColumns);
                    }
        );

        setupColumnLabel();
        setupColumnInstruction();
        setupColumnType();
        setupColumnNum();
        setupColumnCycles();
    }

    private void setupColumnCycles() {
        columnCycles.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getData().getCycles()));

        columnCycles.setCellFactory(col -> new TableCell<InstructionDTO, Integer>() {
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

    private void setupColumnNum() {
        columnNum.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNum()));

        columnNum.setCellFactory(col -> new TableCell<InstructionDTO, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setStyle("-fx-text-fill: #9a9696");
                    setText(String.valueOf(item));
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupColumnType() {
        columnType.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getData().getInstructionType().toString()));

        columnType.setCellFactory(col -> new TableCell<InstructionDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #43974f;");
                    tooltip.setStyle("-fx-font-size: 13");
                    tooltip.setShowDelay(Duration.millis(20));
                    if (item.equals("B")) tooltip.setText("Basic instruction");
                    else if (item.equals("S")) tooltip.setText("Synthetic instruction");
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });

    }

    private void setupColumnInstruction() {
        columnInstruction.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
    }

    private void setupColumnLabel() {
        columnLabel.setCellValueFactory
                (cd -> new ReadOnlyStringWrapper(cd.getValue().getSelfLabel().getLabel()));

        columnLabel.setCellFactory(col -> new TableCell<InstructionDTO, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setStyle("-fx-text-fill: #407abd;");
                    setText(item);
                    tooltip.setStyle("-fx-font-size: 13;");
                    tooltip.setShowDelay(Duration.millis(20));
                    tooltip.setText("Label " + item);
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void fitColumns() {
        ColumnResizer.lockToContent(instructionHistory, 2);
    }

    public void setMainController(AppController mainController) {
        this.mainController =  mainController;

        this.mainController.currentTabControllerProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                // listen to instruction selection in the active ProgramTabController
                newTab.selectedInstructionProperty().addListener
                        ((selObs, oldInstr, selectedInstr) -> {
                    if (selectedInstr != null) {
                        instrHistoryList.clear();
                        List<InstructionDTO> instrList = this.mainController.expandInstr(selectedInstr);
                        instrHistoryList.addAll(instrList);
                    }
                });
            }
        });

        this.mainController.programSwitchedProperty().addListener((obs, was, now) -> {
            instrHistoryList.clear();
        });
    }

    public void setInstrHistoryList(ObservableList<InstructionDTO> instrHistoryList) {
        this.instrHistoryList.set(instrHistoryList);
    }

}