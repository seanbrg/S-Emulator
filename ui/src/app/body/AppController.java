package app.body;

import app.menuBar.MenuBarController;
import app.programTab.ProgramTabController;
import execute.Engine;
import execute.EngineImpl;
import execute.dto.InstructionDTO;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {
    @FXML private HBox menuBarComponent;
    @FXML private MenuBarController menuBarComponentController;
    @FXML private TabPane programTabs;

    // for pointing to the current opened tab:
    private Map<Tab, ProgramTabController> tabControllerMap;
    private ObjectProperty<ProgramTabController> currentTabController;

    private Scene scene;
    private Engine engine;

    @FXML
    public void initialize() {
        if (menuBarComponentController != null) {
            menuBarComponentController.setAppController(this);
        }

        this.currentTabController = new SimpleObjectProperty<>();
        this.tabControllerMap = new HashMap<Tab, ProgramTabController>();
        this.engine = new EngineImpl();
        this.programTabs.getTabs().clear();
        engine.setPrintMode(false);

        // whenever selection changes, update the currentTabController
        programTabs.getSelectionModel()
                .selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                currentTabController.set(tabControllerMap.get(newTab));
            } else {
                currentTabController.set(null);
            }
        });
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        this.menuBarComponentController.setScene(scene);
    }

    public ObjectProperty<ProgramTabController> currentTabControllerProperty() {
        return currentTabController;
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

    @FXML
    public void processFile(String selectedFilePath) {
        boolean result = engine.loadFromXML(selectedFilePath);

        String programName = engine.getProgramName();
        int maxDegree = engine.maxDegree();
        if (result && programName != null) {
            Platform.runLater(() -> {
                addProgramTab(programName);
            });
        } else {
            System.out.println("Failed to load program from: " + selectedFilePath);
        }
    }

    private void addProgramTab(String programName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/app/programTab/programTab.fxml");
            fxmlLoader.setLocation(url);
            Tab programTab = fxmlLoader.load();

            ProgramTabController tabController = fxmlLoader.getController();
            tabController.setProgramName(programName);
            tabController.setMainController(this);
            List<InstructionDTO> list = engine.getInstructionsList(programName, 0);
            tabController.setInstructionsList(FXCollections.observableList(list));

            this.programTabs.getTabs().add(programTab);
            this.tabControllerMap.put(programTab, tabController);
            this.currentTabControllerProperty().set(tabController);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
