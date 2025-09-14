package app.components.body;

import app.components.instructionHistory.InstructionHistoryController;
import app.components.menuBar.MenuBarController;
import app.components.programTab.ProgramTabController;
import app.components.runHistory.RunHistoryController;
import app.components.runWindow.RunWindowController;
import execute.Engine;
import execute.EngineImpl;
import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import execute.dto.VariableDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {
    @FXML private HBox menuBarComponent;
    @FXML private MenuBarController menuBarComponentController;
    @FXML private TabPane programTabs;
    @FXML private TableView runHistory;
    @FXML private RunHistoryController runHistoryController;
    @FXML private TableView instructionHistory;
    @FXML private InstructionHistoryController instructionHistoryController;
    @FXML private BorderPane runWindow;
    @FXML private RunWindowController runWindowController;

    // for pointing to the current opened tab:
    private Map<Tab, ProgramTabController> tabControllerMap;
    private ObjectProperty<ProgramTabController> currentTabController;
    private ReadOnlyBooleanWrapper switchingProgram;

    private ListProperty<VariableDTO> currentRawProgramInputs;
    private ListProperty<VariableDTO> currentActualProgramInputs;
    private IntegerProperty programCycles;
    private Scene scene;
    private Engine engine;

    @FXML
    public void initialize() {
        this.currentTabController = new SimpleObjectProperty<>();
        this.tabControllerMap = new HashMap<Tab, ProgramTabController>();
        this.currentRawProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentActualProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.switchingProgram = new ReadOnlyBooleanWrapper(false);
        this.engine = new EngineImpl();
        this.programTabs.getTabs().clear();
        this.programCycles = new SimpleIntegerProperty(0);

        engine.setPrintMode(false);
        runWindowController.setMainController(this);
        menuBarComponentController.setMainController(this);
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
        Bindings.bindContent(currentActualProgramInputs, runWindowController.actualInputVariablesProperty());

        runWindowController.runningProperty().addListener((obs, was, is) -> {
            if (is) {
                runProgram();
                runWindowController.runningProperty().set(false); // ready for next run
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
        System.out.println(inputs.stream().map(v -> v.getName() + "=" + v.getValue()).toList());
        HistoryDTO result = engine.runProgramAndRecord(programName, degree, inputs);
        if (result == null) throw new RuntimeException("Run failed for program: " + programName);

        programCycles.set(result.getCycles());
        runWindowController.setOutputVariables(result.getOutputs());
        runHistoryController.addRunHistory(result);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        this.menuBarComponentController.setScene(scene);
    }

    public ObjectProperty<ProgramTabController> currentTabControllerProperty() {
        return currentTabController;
    }

    public ReadOnlyBooleanProperty programSwitchedProperty() {
        return switchingProgram.getReadOnlyProperty();
    }

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

    public Task<String> createLoadTask(String filePath) {
        return new Task<>() {
            @Override protected String call() {
                if (!engine.loadFromXML(filePath)) {
                    throw new RuntimeException("Load failed: " + filePath);
                }
                return engine.getProgramName();
            }
        };
    }

    public void newProgram(String programName, int degree) {
        switchingProgram.set(!switchingProgram.get());
        programTabs.getTabs().clear();
        addProgramTab(programName, degree);
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
            List<InstructionDTO> list = engine.getInstructionsList(programName, 0);
            tabController.setInstructionsList(FXCollections.observableList(list));

            this.programTabs.getTabs().add(programTab);
            this.tabControllerMap.put(programTab, tabController);
            this.currentTabControllerProperty().set(tabController);
            refreshInputs();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<InstructionDTO> expandInstr(InstructionDTO selectedInstr) {
        return this.engine.getInstrParents(selectedInstr);
    }

    public ListProperty<VariableDTO> currentRawProgramInputsProperty() {
        return currentRawProgramInputs;
    }

    private void refreshInputs() {
        currentRawProgramInputs.setAll(engine.getInputs()); // or get by program name if needed
    }

    public IntegerProperty runCyclesProperty() {
        return programCycles;
    }
}
