package app.components.body;

import app.components.expandWindow.ExpandWindowController;
import app.components.instructionHistory.InstructionHistoryController;
import app.components.header.headerController;
import app.components.programTab.ProgramTabController;
import app.components.runHistory.RunHistoryController;
import app.components.runMenu.RunMenuController;
import execute.Engine;
import execute.EngineImpl;
import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import execute.dto.LabelDTO;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {
    @FXML private HBox header;
    @FXML private headerController headerController;
    @FXML private TabPane programTabs;
    @FXML private BorderPane runHistory;
    @FXML private RunHistoryController runHistoryController;
    @FXML private TableView instructionHistory;
    @FXML private InstructionHistoryController instructionHistoryController;
    @FXML private BorderPane runMenu;
    @FXML private RunMenuController runMenuController;

    // for pointing to the current opened tab:
    private Map<Tab, ProgramTabController> tabControllerMap;
    private ObjectProperty<ProgramTabController> currentTabController;
    private ReadOnlyBooleanWrapper switchingProgram;

    private ListProperty<VariableDTO> currentRawProgramInputs;
    private ListProperty<VariableDTO> currentActualProgramInputs;
    private ListProperty<VariableDTO> currentVariables;
    private ListProperty<LabelDTO> currentLabels;
    private IntegerProperty programCycles;
    private IntegerProperty debugLine;
    private StringProperty highlightedLabel;
    private StringProperty highlightedVariable;
    private Scene scene;
    private Engine engine;
    private javafx.collections.ObservableSet<Integer> highlightedRows;


    @FXML
    public void initialize() {
        this.currentTabController = new SimpleObjectProperty<>();
        this.tabControllerMap = new HashMap<Tab, ProgramTabController>();
        this.currentRawProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentActualProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentVariables = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentLabels = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.switchingProgram = new ReadOnlyBooleanWrapper(false);
        this.engine = new EngineImpl();
        this.programTabs.getTabs().clear();
        this.programCycles = new SimpleIntegerProperty(0);
        this.debugLine = new SimpleIntegerProperty(0);
        this.highlightedLabel = new SimpleStringProperty("");
        this.highlightedVariable = new SimpleStringProperty("");
        this.highlightedRows = javafx.collections.FXCollections.observableSet(new java.util.HashSet<>());

        engine.setPrintMode(false);
        runMenuController.setMainController(this);
        headerController.setMainController(this);
        runHistoryController.setMainController(this);
        instructionHistoryController.setMainController(this);

        // whenever selection changes, update the currentTabController
        programTabs.getSelectionModel()
                .selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                currentTabController.set(tabControllerMap.get(newTab));
            } else {
                currentTabController.set(null);
            }
            clearHighlights();
        });

        currentTabControllerProperty().addListener((obs, oldC, newC) -> refreshInputs());
        Bindings.bindContent(currentActualProgramInputs, runMenuController.actualInputVariablesProperty());
        currentTabController.addListener((obs, oldC, newC) -> {
            if (oldC != null) {
                Bindings.unbindContent(currentVariables, oldC.getVariablesList());
                Bindings.unbindContent(currentLabels, oldC.getLabelsList());
            }
            if (newC != null) {
                Bindings.bindContent(currentVariables, newC.getVariablesList());
                Bindings.bindContent(currentLabels, newC.getLabelsList());
            } else {
                currentVariables.clear();
                currentLabels.clear();
            }
        });

        runMenuController.runningProperty().addListener((obs, was, is) -> {
            if (is) {
                runProgram();
                runMenuController.runningProperty().set(false); // ready for next run
            }
        });

        runMenuController.debuggingProperty().addListener((obs, was, is) -> {
            if (is) {
                debugStart();
            }
        });
    }

    private void runProgram() {
        ProgramTabController tabController = currentTabController.get();
        if (tabController == null) return;

        String programName = tabController.getProgramName();
        if (programName == null || programName.isEmpty()) return;

        int degree = tabController.getCurrentDegree();

        List<VariableDTO> inputs = currentActualProgramInputs.get();
        HistoryDTO result = engine.runProgramAndRecord(programName, degree, inputs);
        if (result == null) throw new RuntimeException("Run failed for program: " + programName);

        programCycles.set(result.getCycles());
        runMenuController.setOutputVariables(result.getOutputAndTemps());
        runHistoryController.addRunHistory(result);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        this.headerController.setScene(scene);
    }

    public ObjectProperty<ProgramTabController> currentTabControllerProperty() {
        return currentTabController;
    }

    public ReadOnlyBooleanProperty programSwitchedProperty() {
        return switchingProgram.getReadOnlyProperty();
    }

    public IntegerProperty debugLineProperty() { return debugLine; }

    @FXML
    public void switchTheme(boolean dark) {
        if (scene == null) return;

        String cssPath = dark ?
                "/app/resources/styles/style-dark.css" :
                "/app/resources/styles/style-light.css";

        URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public Task<List<String>> createLoadTask(String filePath) {
        return new Task<>() {
            @Override protected List<String> call() {
                if (!engine.loadFromXML(filePath)) {
                    throw new RuntimeException("Load failed: " + filePath);
                }
                //engine.clear();
                return engine.getFuncNamesList();
            }
        };
    }

    public void newProgram(List<String> funcNames) {
        switchingProgram.set(!switchingProgram.get());
        programTabs.getTabs().clear();
        for (String func : funcNames) {
            addProgramTab(func, 0);
        }

        Tab programTab = programTabs.getTabs().getFirst();
        ProgramTabController tabController = tabControllerMap.get(programTab);

        this.programTabs.getSelectionModel().select(programTab);
        this.currentTabControllerProperty().set(tabController);
        refreshInputs();

    }

    private void addProgramTab(String programName, int degree) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/app/components/programTab/programTab.fxml");
            fxmlLoader.setLocation(url);
            Tab programTab = fxmlLoader.load();

            ProgramTabController tabController = fxmlLoader.getController();
            tabController.setProgram(programName, degree, engine.maxDegree(programName));
            tabController.setMainController(this);
            List<InstructionDTO> instrList = engine.getInstructionsList(programName, degree);
            List<VariableDTO> varList = engine.getOutputs(programName, degree);
            List<LabelDTO> inputList = engine.getLabels(programName, degree);
            tabController.setInstructionsList(FXCollections.observableList(instrList));
            tabController.setVariablesList(FXCollections.observableList(varList));
            tabController.setLabelsList(FXCollections.observableList(inputList));

            this.programTabs.getTabs().add(programTab);
            this.tabControllerMap.put(programTab, tabController);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void expandProgram() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource(
                    "/app/components/expandWindow/expandWindow.fxml"));

            Parent root = fx.load();
            ExpandWindowController c = fx.getController();
            String funcName = currentTabController.get().getProgramName();
            int maxDegree = engine.maxDegree(funcName);
            c.setMaxExpansion(maxDegree);

            // show modally (main window disabled) and wait
            Stage s = new Stage();
            s.initOwner(scene.getWindow());
            s.initModality(Modality.APPLICATION_MODAL);
            s.setTitle("Choose Expansion Degree");
            Scene dialogScene = new Scene(root);
            dialogScene.getStylesheets().addAll(scene.getStylesheets()); // inherit theme
            s.getIcons().add(new Image(getClass().getResourceAsStream("/app/resources/images/icon.png")));
            s.setScene(dialogScene);
            s.setResizable(false);
            s.showAndWait();

            // read the result (0 == cancelled)
            int chosenDegree = c.getResult();
            if (chosenDegree > 0) {
                addProgramTab(funcName, chosenDegree);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public List<InstructionDTO> expandInstr(InstructionDTO selectedInstr) {
        return this.engine.getInstrParents(selectedInstr);
    }

    public ListProperty<VariableDTO> currentRawProgramInputsProperty() {
        return currentRawProgramInputs;
    }

    private void refreshInputs() {
        ProgramTabController tabController = currentTabController.get();
        if (tabController == null) return;

        String programName = tabController.getProgramName();
        if (programName == null || programName.isEmpty()) return;

        int degree = tabController.getCurrentDegree();

        currentRawProgramInputs.setAll(engine.getInputs(programName, degree)); // or get by program name if needed
    }

    public IntegerProperty runCyclesProperty() {
        return programCycles;
    }

    public void debugStart() {
        ProgramTabController tabController = currentTabController.get();
        if (tabController == null) return;

        String programName = tabController.getProgramName();
        if (programName == null || programName.isEmpty()) return;

        int degree = tabController.getCurrentDegree();
        List<VariableDTO> inputs = currentActualProgramInputs.get();
        debugLine.set(0);
        engine.debugStart(programName, degree, inputs);
    }



    public Boolean debugStep() {
        ProgramTabController tabController = currentTabController.get();
        if (tabController == null) return false;

        String programName = tabController.getProgramName();
        if (programName == null || programName.isEmpty()) return false;

        int degree = tabController.getCurrentDegree();
        debugLine.set(engine.getDebugLine());
        replaceHighlights(List.of(debugLine.get()));
        Boolean notDone = engine.debugStep(programName, degree);

        // Get ALL variables (outputs, inputs, temps) for debugging display
        List<List<VariableDTO>> varsByType = engine.getVarByType();
        List<VariableDTO> allVariables = new java.util.ArrayList<>();
        for (List<VariableDTO> varList : varsByType) {
            allVariables.addAll(varList);
        }
        runMenuController.setOutputVariables(allVariables);

        if (!notDone) {clearHighlights();}
        return notDone;
    }


    public javafx.collections.ObservableSet<Integer> highlightedRowsProperty() {
        return highlightedRows;
    }
    public javafx.collections.ObservableSet<Integer> getHighlightedRows() {
        return highlightedRows;
    }

    // Convenience methods for callers (any controller/service can use these):
    public void highlightRow(int line) { highlightedRows.add(line); }
    public void unhighlightRow(int line) { highlightedRows.remove(line); }
    public void replaceHighlights(java.util.Collection<Integer> lines) {
        highlightedRows.clear();
        highlightedRows.addAll(lines);
    }
    public void clearHighlights() { highlightedRows.clear(); }

    public void alertLoadFailed() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Failed");
            alert.setHeaderText("Failed to load program file!");
            alert.setContentText("Please check the file format and try again.");

            // Use main window as owner if available
            if (scene != null && scene.getWindow() != null) {
                alert.initOwner(scene.getWindow());
                // Inherit current theme
                alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
            }

            // Use app icon on the dialog window (if available)
            try {
                Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
                dlg.getIcons().add(new Image(getClass().getResourceAsStream("/app/resources/images/icon.png")));
            } catch (Exception ignored) { /* icon optional */ }

            alert.showAndWait();
        });
    }

    public void finishDebugging() {
        String programName = currentTabController.get().getProgramName();
        int degree = currentTabController.get().getCurrentDegree();
        List<VariableDTO> inputs = currentActualProgramInputs.get();

        HistoryDTO result = engine.recordCurrentState(programName, degree, inputs);
        if (result == null) throw new RuntimeException("Run failed for program: " + programName);

        programCycles.set(result.getCycles());
        runMenuController.setOutputVariables(result.getOutputAndTemps());
        runHistoryController.addRunHistory(result);
    }

    public void prepareRerun(HistoryDTO history) {
        if (history == null) return;

        String programName = history.getProgram().getProgramName();
        int degree = history.getDegree();

        // Step 1: Switch to the correct program/degree tab
        Tab targetTab = findOrCreateProgramTab(programName, degree);
        if (targetTab == null) return;

        programTabs.getSelectionModel().select(targetTab);
        ProgramTabController tabController = tabControllerMap.get(targetTab);
        currentTabController.set(tabController);

        // Step 2: FIRST clear the edited values in RunMenuController
        runMenuController.clearEditedValues();

        // Step 3: Refresh to get the default inputs for this program/degree
        refreshInputs();

        // Step 4: Load inputs from the selected run and update immediately
        List<VariableDTO> previousInputs = history.getInputs();

        // Create a map of input name -> value from history
        Map<String, Long> historyInputValues = new HashMap<>();
        for (VariableDTO input : previousInputs) {
            historyInputValues.put(input.getName(), input.getValue());
        }

        // Update current inputs with values from history
        List<VariableDTO> updatedInputs = currentRawProgramInputs.stream()
                .map(v -> new VariableDTO(v.getName(),
                        historyInputValues.getOrDefault(v.getName(), v.getValue())))
                .toList();

        // Set the updated inputs BEFORE Platform.runLater
        currentRawProgramInputs.setAll(updatedInputs);

        // Step 5: Clear previous run state and UI updates
        Platform.runLater(() -> {
            // Reset states
            runMenuController.runningProperty().set(false);
            runMenuController.debuggingProperty().set(false);

            // Clear highlights and console
            clearHighlights();
            runMenuController.clearLog();

            // Clear outputs and cycles
            runMenuController.clearOutputs();
            programCycles.set(0);
            debugLine.set(0);

            // Force table refresh
            runMenuController.refreshInputTable();

            // Step 6: Log the re-run preparation
            runMenuController.log("=== Re-Run Prepared ===");
            runMenuController.log("Program: " + programName + " (Degree: " + degree + ")");
            runMenuController.log("Inputs loaded from Run #" + history.getNum());

            // Log the actual input values for verification
            for (VariableDTO input : updatedInputs) {
                runMenuController.log("  " + input.getName() + " = " + input.getValue());
            }

            runMenuController.log("You can now modify inputs and click Run or Debug.");
        });
    }

    /**
     * Find existing tab or create new one for the specified program/degree
     */
    private Tab findOrCreateProgramTab(String programName, int degree) {
        // First, try to find existing tab with matching program and degree
        for (Map.Entry<Tab, ProgramTabController> entry : tabControllerMap.entrySet()) {
            ProgramTabController controller = entry.getValue();
            if (controller.getProgramName().equals(programName) &&
                    controller.getCurrentDegree() == degree) {
                return entry.getKey();
            }
        }

        // If not found, create new tab with the required degree
        addProgramTab(programName, degree);

        // Find the newly created tab
        for (Map.Entry<Tab, ProgramTabController> entry : tabControllerMap.entrySet()) {
            ProgramTabController controller = entry.getValue();
            if (controller.getProgramName().equals(programName) &&
                    controller.getCurrentDegree() == degree) {
                return entry.getKey();
            }
        }

        return null;
    }
}
