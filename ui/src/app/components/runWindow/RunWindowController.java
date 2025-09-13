package app.components.runWindow;

import app.components.body.AppController;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class RunWindowController {
    @FXML private AppController mainController;

    @FXML private Button buttonRun;
    @FXML private Button buttonDebug;
    @FXML private ListView<VariableDTO> resultsList;
    @FXML private TableView<VariableDTO> inputsTable;
    @FXML private TableColumn<VariableDTO, String> varColumn;
    @FXML private TableColumn<VariableDTO, Integer> valueColumn;


    private ListProperty<VariableDTO> inputVariablesRaw;
    private ListProperty<VariableDTO> inputVariables; // processed user inputs
    private ListProperty<VariableDTO> outputVariables;

    @FXML
    public void initialize() {
        buttonRun.setOnAction(event -> handleRun());
        buttonDebug.setOnAction(event -> handleDebug());
        inputVariablesRaw = new SimpleListProperty<>();
        inputVariables = new SimpleListProperty<>();
        outputVariables = new SimpleListProperty<>();

        Platform.runLater(() -> {
            buttonRun.disableProperty().bind(
                    mainController.currentTabControllerProperty().isNull()
            );

            buttonDebug.disableProperty().bind(
                    mainController.currentTabControllerProperty().isNull()
            );
        });

        inputsTable.getColumns().setAll(varColumn, valueColumn);

        // fit once after first layout, then refit when items change
        Platform.runLater(this::lockVarWidth);
        inputsTable.getItems().addListener(
                (javafx.collections.ListChangeListener<? super VariableDTO>) c ->
                        javafx.application.Platform.runLater(this::lockVarWidth)
        );


        // TableView <- inputVariables
        inputsTable.itemsProperty().bind(inputVariablesRaw);
        varColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        valueColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<Integer>((int) cd.getValue().getValue()));

        // ListView <- outputVariables
        resultsList.itemsProperty().bind(outputVariables);
        resultsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(VariableDTO v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.getName() + " = " + v.getValue());
            }
        });

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

    private void handleRun() {
        // TODO: Implement run functionality
    }

    private void handleDebug() {
        // TODO: Implement debug functionality
    }

    public void setMainController(AppController appController) {
        mainController = appController;

        inputVariablesRaw.bind(mainController.currentProgramInputsProperty());
    }
}
