package app.components.programTab;

import app.util.ColumnResizer;
import app.components.body.AppController;
import execute.dto.InstructionDTO;
import execute.dto.LabelDTO;
import execute.dto.VariableDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.List;

public class ProgramTabController {
    @FXML private AppController mainController;

    @FXML private Tab programTab;
    @FXML private TableView<InstructionDTO> programTable;
    @FXML private TableColumn<InstructionDTO, String> columnLabel;
    @FXML private TableColumn<InstructionDTO, String> columnInstruction;
    @FXML private TableColumn<InstructionDTO, String> columnType;
    @FXML private TableColumn<InstructionDTO, Integer> columnNum;
    @FXML private TableColumn<InstructionDTO, Integer> columnCycles;

    private String programName;
    int degree;
    private ListProperty<InstructionDTO> instructions;
    private ListProperty<VariableDTO> variables;
    private ListProperty<LabelDTO> labels;

    private static final javafx.css.PseudoClass HIGHLIGHTED =
            javafx.css.PseudoClass.getPseudoClass("highlighted");

    @FXML
    public void initialize() {
        this.instructions = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.variables = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.labels = new SimpleListProperty<>(FXCollections.observableArrayList());

        programTable.itemsProperty().bind(instructions);

        programTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fitColumns();
        programTable.widthProperty().addListener((o, a, b) -> fitColumns());
        programTable.getItems().addListener(
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
                    tooltip.setShowDelay(Duration.millis(500));
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
                    tooltip.setShowDelay(Duration.millis(500));
                    tooltip.setText("Label " + item);
                    setTooltip(tooltip);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void fitColumns() {
        ColumnResizer.lockToContent(programTable, 2);
    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        enableRowHighlighting(mainController);
    }

    public void enableRowHighlighting(AppController appController) {

        var sharedHighlights = appController.getHighlightedRows();

        programTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<InstructionDTO>();

            Runnable apply = () -> {
                var item = row.getItem();
                boolean on = false;
                if (item != null && !row.isEmpty()) {
                    int line = item.getNum();
                    on = sharedHighlights.contains(line);
                }
                row.pseudoClassStateChanged(HIGHLIGHTED, on);
            };

            // Update when the row’s item changes
            row.itemProperty().addListener((obs, oldV, newV) -> apply.run());

            // Update when the shared highlight set changes
            sharedHighlights.addListener((javafx.collections.SetChangeListener<Integer>) ch -> {
                // Fast path: only refresh this row if its key matches the changed element,
                // but recalculating is cheap—so we can just:
                apply.run();
            });

            return row;
        });
    }


    public void setProgram(String programName, int degree) {
        this.programName = programName;
        this.degree = degree;
        this.programTab.setText(this.programName + " (" + this.degree + ")");
    }

    public Tab getTab() { return programTab; }

    public void setInstructionsList(ObservableList<InstructionDTO> instructionsList) {
        this.instructions.set(instructionsList);
    }

    public void setVariablesList(ObservableList<VariableDTO> variablesList) {
        this.variables.set(variablesList);
    }

    public void setLabelsList(ObservableList<LabelDTO> labelsList) {
        this.labels.set(labelsList);
    }

    public ListProperty<VariableDTO> getVariablesList() { return variables; }

    public ListProperty<LabelDTO> getLabelsList() { return labels; }

    public ReadOnlyObjectProperty<InstructionDTO> selectedInstructionProperty() {
        return programTable.getSelectionModel().selectedItemProperty();
    }

    public String getProgramName() {
        return programName;
    }

    public int getCurrentDegree() {
        return degree;
    }

    public TableView<InstructionDTO> getInstructionsTable() {
        return programTable;
    }
}
