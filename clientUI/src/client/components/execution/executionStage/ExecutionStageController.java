package client.components.execution.executionStage;

import client.components.execution.expandWindow.ExpandWindowController;
import client.components.execution.instructionHistory.InstructionHistoryController;
import client.components.header.HeaderController;
import client.components.execution.programTab.ProgramTabController;
import client.components.execution.runHistory.RunHistoryController;
import client.components.execution.runMenu.RunMenuController;
import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.servlets.DebugServlet;
import emulator.utils.WebConstants;
import execute.EngineImpl;
import execute.dto.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
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
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class ExecutionStageController {
    @FXML private HBox header;
    @FXML private HeaderController headerController;
    @FXML private TabPane programTabs;
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
    private ObservableSet<Integer> highlightedRows;
    private static final Gson GSON = new Gson();


    @FXML
    public void initialize() {
        this.currentTabController = new SimpleObjectProperty<>();
        this.tabControllerMap = new HashMap<Tab, ProgramTabController>();
        this.currentRawProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentActualProgramInputs = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentVariables = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.currentLabels = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.switchingProgram = new ReadOnlyBooleanWrapper(false);
        this.programTabs.getTabs().clear();
        this.programCycles = new SimpleIntegerProperty(0);
        this.debugLine = new SimpleIntegerProperty(0);
        this.highlightedLabel = new SimpleStringProperty("");
        this.highlightedVariable = new SimpleStringProperty("");
        this.highlightedRows = FXCollections.observableSet(new HashSet<>());

        runMenuController.setMainController(this);
        headerController.setMainController(this);
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
        String encodedName = URLEncoder.encode(programName, StandardCharsets.UTF_8);
        String runUrl = WebConstants.RUN_URL +
                "?" + WebConstants.PROGRAM_NAME + "=" + encodedName +
                "&" + WebConstants.PROGRAM_DEGREE + "=" + degree;
        RequestBody requestBody = RequestBody.create(
                GSON.toJson(inputs),
                MediaType.parse("application/json")
        );

        HttpUtils.postAsync(runUrl, requestBody).thenAccept(json -> {
            HistoryDTO result = GSON.fromJson(json, HistoryDTO.class);
            Platform.runLater(() -> {
                if (result == null) {
                    throw new RuntimeException("Run failed for program: " + programName);
                }

                programCycles.set(result.getCycles());
                runMenuController.setOutputVariables(result.getOutputAndTemps());
            });
        });
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
                "/client/resources/styles/style-dark.css" :
                "/client/resources/styles/style-light.css";

        URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public Task<List<String>> createLoadTask(String filePath) {
        return new Task<>() {
            @Override
            protected List<String> call() throws IOException {
                String uploadUrl = WebConstants.PROGRAMS_URL;
                String listUrl = WebConstants.PROGRAMS_LIST_URL;

                File file = new File(filePath);
                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/xml");
                    }
                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        try (FileInputStream in = new FileInputStream(file)) {
                            sink.writeAll(Okio.source(in));
                        }
                    }
                };

                try (Response r = HttpUtils.postSync(uploadUrl, requestBody)) {
                    if (!r.isSuccessful()) {
                        System.out.println("[DEBUG] POST status=" + r.code() + " location=" + r.header("Location"));
                        System.out.println("[DEBUG] POST body=" + (r.body() != null ? r.body().string() : "<no body>"));
                        throw new IOException("Upload failed: " + r.code());
                    }
                }
                List<String> funcNames;
                try (Response r = HttpUtils.getSync(listUrl)) {
                    if (!r.isSuccessful() || r.body() == null) {
                        System.out.println("[DEBUG] POST status=" + r.code() + " location=" + r.header("Location"));
                        System.out.println("[DEBUG] POST body=" + (r.body() != null ? r.body().string() : "<no body>"));
                        throw new IOException("List fetch failed: " + r.code());
                    }
                    String json = r.body().string();
                    funcNames = GSON.fromJson(json, new TypeToken<List<String>>(){}.getType());
                }
                return funcNames;
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
        // Build URLs (encode the name to be safe)
        String encodedName = URLEncoder.encode(programName, StandardCharsets.UTF_8);
        String pullFuncUrl = WebConstants.PROGRAMS_URL +
                "?" + WebConstants.PROGRAM_NAME + "=" + encodedName +
                "&" + WebConstants.PROGRAM_DEGREE + "=" + degree;
        String pullVarsUrl = pullFuncUrl + "&" + WebConstants.PROGRAM_VARLIST + "=true";

        // 1) Async fetch both endpoints in parallel
        CompletableFuture<String> funcJsonFut = HttpUtils.getAsync(pullFuncUrl);
        CompletableFuture<String> varsJsonFut = HttpUtils.getAsync(pullVarsUrl);

        // 2) When both are ready, parse and construct the UI data (off UI thread)
        funcJsonFut.thenCombine(varsJsonFut, (funcJson, varsJson) -> {
                    ProgramDTO programDTO = GSON.fromJson(funcJson, ProgramDTO.class);
                    List<VariableDTO> varList = GSON.fromJson(varsJson, new TypeToken<List<VariableDTO>>() {}.getType());

                    return new Object[] { programDTO, varList };
                })
                // 3) Now update the UI
                .thenAccept(result -> Platform.runLater(() -> {
                    try {
                        ProgramDTO programDTO = (ProgramDTO) result[0];
                        @SuppressWarnings("unchecked")
                        List<VariableDTO> varList = (List<VariableDTO>) result[1];

                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getResource("/client/components/execution/programTab/programTab.fxml"));
                        Tab programTab = fxmlLoader.load();
                        ProgramTabController tabController = fxmlLoader.getController();

                        tabController.setProgram(programName, degree, programDTO.getMaxDegree());
                        tabController.setMainController(this);

                        List<InstructionDTO> instrList = programDTO.getInstructions();
                        List<LabelDTO> inputList = programDTO.getLabels().keySet().stream().map(LabelDTO::new).toList();

                        tabController.setInstructionsList(FXCollections.observableList(instrList));
                        tabController.setVariablesList(FXCollections.observableList(varList));
                        tabController.setLabelsList(FXCollections.observableList(inputList));

                        this.programTabs.getTabs().add(programTab);
                        this.tabControllerMap.put(programTab, tabController);
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }));
    }

    public void expandProgram() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource(
                    "/client/components/execution/expandWindow/expandWindow.fxml"));

            Parent root = fx.load();
            ExpandWindowController c = fx.getController();
            String funcName = currentTabController.get().getProgramName();

            String encodedName = URLEncoder.encode(funcName, StandardCharsets.UTF_8);
            String maxDegreeUrl = WebConstants.MAXDEGREE_URL +
                    "?" + WebConstants.PROGRAM_NAME + "=" + encodedName;

            CompletableFuture<String> maxDegreeFut = HttpUtils.getAsync(maxDegreeUrl);
            maxDegreeFut.thenAccept(result -> Platform.runLater(() -> {

                int maxDegree = GSON.fromJson(result, Integer.class);

                int currentDegree = currentTabController.get().getCurrentDegree();
                int realMaxDegree = maxDegree - currentDegree;
                c.setMaxExpansion(realMaxDegree);

                // show modally (main window disabled) and wait
                Stage s = new Stage();
                s.initOwner(scene.getWindow());
                s.initModality(Modality.APPLICATION_MODAL);
                s.setTitle("Choose Expansion Degree");
                Scene dialogScene = new Scene(root);
                dialogScene.getStylesheets().addAll(scene.getStylesheets()); // inherit theme
                s.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
                s.setScene(dialogScene);
                s.setResizable(false);
                s.showAndWait();

                // read the result (0 == cancelled)
                int chosenDegree = c.getResult();
                if (chosenDegree > 0) {
                    addProgramTab(funcName, chosenDegree);
                }

            }));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public List<InstructionDTO> expandInstr(InstructionDTO selectedInstr) {
        return EngineImpl.getInstrParents(selectedInstr);
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

        String encodedName = URLEncoder.encode(programName, StandardCharsets.UTF_8);
        String getInputsUrl = WebConstants.INPUTS_URL +
                "?" + WebConstants.PROGRAM_NAME + "=" + encodedName +
                "&" + WebConstants.PROGRAM_DEGREE + "=" + degree;

        CompletableFuture<String> inputsFut = HttpUtils.getAsync(getInputsUrl);
        inputsFut.thenAccept(inputsJson -> {
            List<VariableDTO> inputs = GSON.fromJson(
                    inputsJson,
                    new TypeToken<List<VariableDTO>>() {}.getType());

            Platform.runLater(() -> currentRawProgramInputs.setAll(inputs));
            });
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

        String encodedName = URLEncoder.encode(programName, StandardCharsets.UTF_8);
        String debugStartUrl = WebConstants.DEBUG_URL +
                "?" + WebConstants.PROGRAM_NAME + "=" + encodedName +
                "&" + WebConstants.PROGRAM_DEGREE + "=" + degree;
        RequestBody requestBody = RequestBody.create(
                GSON.toJson(inputs),
                MediaType.parse("application/json")
        );
        HttpUtils.postAsync(debugStartUrl, requestBody).thenAccept(v -> { debugLine.set(0); });
    }



    public CompletableFuture<Boolean> debugStep() {
        ProgramTabController tabController = currentTabController.get();
        if (tabController == null) {
            return CompletableFuture.completedFuture(false);
        }

        List<VariableDTO> outputs = runMenuController.getOutputVariables();
        RequestBody requestBody = RequestBody.create(
                GSON.toJson(outputs),
                MediaType.parse("application/json"));

        // Assume postAsync returns CompletableFuture<String> with the response body
        return HttpUtils.postAsync(WebConstants.DEBUG_NEXT_URL, requestBody)
                .thenApply(json -> {
                    DebugServlet.DebugInfo info =  GSON.fromJson(json, DebugServlet.DebugInfo.class);

                    int debugLineRes = info.getDebugLine();
                    boolean hasMore = info.isHasMore();
                    List<VariableDTO> updatedOutputs = info.getVariables();

                    // update UI on FX thread
                    Platform.runLater(() -> {
                        debugLine.set(debugLineRes);
                        replaceHighlights(List.of(debugLineRes));
                        runMenuController.setOutputVariables(updatedOutputs);
                        if (!hasMore) clearHighlights();
                    });

                    return hasMore;   // ✅ this value becomes the future’s result
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> System.err.println(ex.getMessage()));
                    return false;
                });
    }


    public ObservableSet<Integer> highlightedRowsProperty() {
        return highlightedRows;
    }
    public ObservableSet<Integer> getHighlightedRows() {
        return highlightedRows;
    }

    // Convenience methods for callers (any controller/service can use these):
    public void highlightRow(int line) { highlightedRows.add(line); }
    public void unhighlightRow(int line) { highlightedRows.remove(line); }
    public void replaceHighlights(Collection<Integer> lines) {
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
                dlg.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
            } catch (Exception ignored) {  }

            alert.showAndWait();
        });
    }

    public void finishDebugging() {
        HttpUtils.getAsync(WebConstants.DEBUG_URL).thenAccept(json -> {
            HistoryDTO result = GSON.fromJson(json, HistoryDTO.class);
            if (result == null) throw new RuntimeException("Run failed for program: " + currentTabController.get().getProgramName());

            Platform.runLater(() -> {
                programCycles.set(result.getCycles());
                runMenuController.setOutputVariables(result.getOutputAndTemps());
            });
        });
    }

    public RunMenuController getRunMenuController() {
        return runMenuController;
    }
}
