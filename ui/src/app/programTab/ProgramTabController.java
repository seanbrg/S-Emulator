package app.programTab;

import app.body.AppController;
import execute.dto.InstructionDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.Text;
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
    // TODO: add breakpoint column

    private String programName;
    private ListProperty<InstructionDTO> instructions;

    @FXML
    public void initialize() {
        this.instructions = new SimpleListProperty<>(FXCollections.observableArrayList());
        programTable.itemsProperty().bind(instructions);

        programTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        programTable.getItems().addListener((ListChangeListener<Object>) c -> {
            autoResizeColumns(programTable);
        });

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


    public void setMainController(AppController mainController) { this.mainController = mainController; }

    public void setProgramName(String programName) {
        this.programName = programName;
        this.programTab.setText(this.programName);
    }

    public Tab getTab() { return programTab; }

    public void setInstructionsList(ObservableList<InstructionDTO> instructionsList) {
        this.instructions.set(instructionsList);
    }

    public List<InstructionDTO> getInstructionsList() { return instructions.get(); }
}
