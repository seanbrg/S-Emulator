package app.programTab;

import app.AppController;
import execute.Engine;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class ProgramTabController {
    @FXML private Tab programTab;
    @FXML private AppController mainController;

    private Engine engine;
    private String programName;

    @FXML
    public void initialize() {

    }

    public void setMainController(AppController mainController) { this.mainController = mainController; }

    public void setEngine(Engine engine) { this.engine = engine; }

    public void setProgramName(String programName) {
        this.programName = programName;
        this.programTab.setText(programName);
    }

    public Tab getTab() {
        return programTab;
    }
}
