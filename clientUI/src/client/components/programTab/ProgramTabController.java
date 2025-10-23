package client.components.programTab;

import client.util.ColumnResizer;
import execute.dto.InstructionDTO;
import execute.dto.LabelDTO;
import execute.dto.VariableDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ProgramTabController {
    @FXML private client.components.body.DashboardController mainController;

    @FXML private Tab programTab;
    @FXML private TableView<InstructionDTO> programTable;
    @FXML private TableColumn<InstructionDTO, String> columnLabel;
    @FXML private TableColumn<InstructionDTO, String> columnInstruction;
    @FXML private TableColumn<InstructionDTO, String> columnType;
    @FXML private TableColumn<InstructionDTO, Integer> columnNum;
    @FXML private TableColumn<InstructionDTO, Integer> columnCycles;

    private String programName;
    int degree;
    int maxDegree;
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
                        javafx.application.Platform.runLater(this::fitColumns)
        );

        setupColumnLabel();
        setupColumnInstruction();
        setupColumnType();
        setupColumnNum();
        setupColumnCycles();

        // Make tab closable (native JavaFX close button)
        programTab.setClosable(true);

        programTab.tabPaneProperty().addListener((obs, oldTP, newTP) -> {
            if (newTP != null && newTP.getTabClosingPolicy() == TabPane.TabClosingPolicy.UNAVAILABLE) {
                newTP.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
            }
        });


        programTab.setOnCloseRequest(event -> {
            String nameForDialog = (programName == null ? "this tab" : programName + " (" + degree + "/" + maxDegree + ")");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Close tab " + nameForDialog + "?",
                    ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.setTitle("Confirm Close");
            Scene scene = programTab.getTabPane() != null ? programTab.getTabPane().getScene() : null;

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
            } catch (Exception ignored) { /* icon optional */ }


            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                event.consume(); // cancel close
                return;
            }



        });
    }



    private void setupColumnCycles() {
        columnCycles.setCellValueFactory
                (cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCycles()));

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
        ColumnResizer.lockToContent(programTable, 2);
    }

    public void setMainController(client.components.body.DashboardController mainController) {
        this.mainController = mainController;
        enableRowHighlighting(mainController);
    }

    public void enableRowHighlighting(client.components.body.DashboardController dashboardController) {
        var sharedHighlights = dashboardController.getHighlightedRows();

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

            row.itemProperty().addListener((obs, oldV, newV) -> apply.run());
            sharedHighlights.addListener((javafx.collections.SetChangeListener<Integer>) ch -> apply.run());

            return row;
        });
    }

    public void setProgram(String programName, int degree, int maxDegree) {
        this.programName = programName;
        this.degree = degree;
        this.maxDegree = maxDegree;
        this.programTab.setText(this.programName + " (" + this.degree + "/" + this.maxDegree + ")");
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
