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
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    @FXML private TableView runHistory;
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
        runMenuController.setOutputVariables(result.getOutputs());
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
                engine.clear();
                if (!engine.loadFromXML(filePath)) {
                    throw new RuntimeException("Load failed: " + filePath);
                }
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
            tabController.setProgram(programName, degree);
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
            String funcName = currentTabController.getName();
            //TODO: FIX BUG WITH FUNCNAME BEING NULL

            // give the window its max degree
            int maxDegree = engine.maxDegree(funcName);      // existing API
            c.setMaxExpansion(maxDegree);            // existing controller API

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
        Boolean notDone = engine.debugStep(programName, degree);
        runMenuController.setOutputVariables(engine.getOutputs(programName, degree));

        return notDone;
    }

    public StringProperty highlightedLabelProperty() {
        return highlightedLabel;
    }

    public StringProperty highlightedVariableProperty() {
        return highlightedVariable;
    }
}
