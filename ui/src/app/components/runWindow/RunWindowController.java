package app.components.runWindow;

import app.components.body.AppController;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunWindowController {
    @FXML public VBox vBox;
    @FXML private AppController mainController;

    @FXML private Button buttonRun;
    @FXML private Button buttonDebug;
    @FXML private ListView<VariableDTO> resultsList;
    @FXML private TableView<VariableDTO> inputsTable;
    @FXML private TableColumn<VariableDTO, String> varColumn;
    @FXML private TableColumn<VariableDTO, Long> valueColumn;
    @FXML private TextArea console;


    private ListProperty<VariableDTO> inputVariablesRaw;
    private ReadOnlyListWrapper<VariableDTO> ActualInputVariables;
    private ReadOnlyListWrapper<VariableDTO> outputVariables;
    private Map<String, Long> editedValues;
    private BooleanProperty running;
    private BooleanProperty debugging;


    @FXML
    public void initialize() {
        vBox.getStyleClass().add("darker-vbox");

        buttonRun.setOnAction(event -> handleRun());
        buttonDebug.setOnAction(event -> handleDebug());
        inputVariablesRaw = new SimpleListProperty<>();
        ActualInputVariables = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
        outputVariables = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
        editedValues = new HashMap<>();
        running = new SimpleBooleanProperty(false);
        debugging = new SimpleBooleanProperty(false);

        inputsTable.getColumns().setAll(varColumn, valueColumn);

        // fit once after first layout, then refit when items change
        Platform.runLater(this::lockVarWidth);
        inputsTable.getItems().addListener(
                (javafx.collections.ListChangeListener<? super VariableDTO>) c ->
                        javafx.application.Platform.runLater(this::lockVarWidth)
        );


        inputsTable.itemsProperty().bind(inputVariablesRaw);

        // TableView <- inputVariables
        varColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        valueColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getValue()));

        // align bottom-center and tag this column for CSS
        varColumn.getStyleClass().add("var-col");   // tag for CSS
        varColumn.setStyle("-fx-alignment: CENTER;"); // exact middle (both axes)

        // ListView <- outputVariables
        resultsList.itemsProperty().bind(outputVariables);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(VariableDTO v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.getName() + " = " + v.getValue());
            }
        });

        inputsTable.setEditable(true);
        valueColumn.setEditable(true);

        valueColumn.setCellFactory(col -> new TableCell<VariableDTO, Long>() {
            private final TextField tf = new TextField();

            {
                // only digits
                tf.setTextFormatter(new TextFormatter<String>(change ->
                        change.getControlNewText().matches("\\d*") ? change : null
                ));
                // commit on Enter or focus loss
                tf.setOnAction(e -> commit());
                tf.focusedProperty().addListener((obs, was, is) -> { if (!is) commit(); });
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                tf.setPrefWidth(Double.MAX_VALUE);
            }

            private void commit() {
                VariableDTO row = getTableRow() == null ? null : getTableRow().getItem();
                if (row == null) return;
                String s = tf.getText();
                if (s == null || s.isEmpty()) {
                    editedValues.remove(row.getName());
                    return;
                }
                editedValues.put(row.getName(), Long.parseLong(s));
            }

            @Override protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                VariableDTO row = getTableRow() == null ? null : getTableRow().getItem();
                Long shown = (row != null && editedValues.containsKey(row.getName()))
                        ? editedValues.get(row.getName()) : item;
                tf.setText(shown == null ? "" : String.valueOf(shown));
                setGraphic(tf);
            }
        });


    }

    public void setMainController(AppController appController) {
        mainController = appController;

        inputVariablesRaw.bind(mainController.currentRawProgramInputsProperty());

        buttonRun.disableProperty().bind(
                mainController.currentTabControllerProperty().isNull()
        );

        buttonDebug.disableProperty().bind(
                mainController.currentTabControllerProperty().isNull()
        );

        this.mainController.programSwitchedProperty().addListener((obs, was, now) -> {
            inputVariablesRaw.clear();
            outputVariables.clear();
            editedValues.clear();
            clearLog();
        });

        mainController.runCyclesProperty().addListener((o, oldV, newV) ->
                log(newV + " cycles requested.")
        );
    }

    private void lockVarWidth() {
        double w = contentWidth(varColumn) + 5;
        varColumn.setMinWidth(w);
        varColumn.setPrefWidth(w);
        varColumn.setMaxWidth(w);
        // valueColumn stays resizable (default) and, under CONSTRAINED policy, takes the remaining width
    }


    private double contentWidth(TableColumn<?, ?> col) {
        double max = textW(col.getText());
        for (int i = 0; i < inputsTable.getItems().size(); i++) {
            Object v = col.getCellData(i);
            if (v != null) max = Math.max(max, textW(String.valueOf(v)));
        }
        return Math.ceil(max);
    }

    private double textW(String s) {
        return new Text(s == null ? "" : s).getLayoutBounds().getWidth();
    }

    @FXML
    private void handleRun() {
        Platform.runLater(() -> {
            clearLog();
            rebuildInputsFromTable();
            running.set(true);
        });
    }

    public BooleanProperty runningProperty() {
        return running;
    };

    public ReadOnlyListProperty<VariableDTO> actualInputVariablesProperty() {
        return ActualInputVariables;
    }

    public ReadOnlyListProperty<VariableDTO> outputVariablesProperty() {
        return outputVariables.getReadOnlyProperty();
    }

    public void log(String line) {
        javafx.application.Platform.runLater(() -> {
            console.appendText(line + System.lineSeparator());
            console.positionCaret(console.getLength()); // auto-scroll
        });
    }

    public void clearLog() {
        javafx.application.Platform.runLater(() -> console.clear());
    }

    private void rebuildInputsFromTable() {
        var rebuilt = inputsTable.getItems().stream()
                .map(v -> copyWithValue(v, editedValues.getOrDefault(v.getName(), v.getValue())))
                .toList();
        ActualInputVariables.clear();
        ActualInputVariables.setAll(rebuilt);   // now SAFE (backing list is modifiable)
    }

    // adjust the constructor to your actual DTO shape
    private VariableDTO copyWithValue(VariableDTO src, long newValue) {
        return new VariableDTO(src.getName(), newValue);
    }

    private void handleDebug() {
        // TODO: Implement debug functionality
    }

    public void setOutputVariables(List<VariableDTO> outputs) {
        outputVariables.clear();
        outputVariables.addAll(outputs);
    }
}
