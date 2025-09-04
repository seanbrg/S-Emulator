package app.header;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;

public class HeaderController {
    private AppController mainController;

    @FXML private Button loadFileButton;
    @FXML private Label fileLoadedLabel;
    @FXML private Label degreeLabel;
    @FXML private Button expandButton;
    @FXML private Button collapseButton;
    @FXML private ComboBox<String> selectProgram;

    private int expandedDegree = 0;
    private int maxDegree = 0;

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> handleFileSelection());
        expandButton.setOnAction(event -> expandDegree());
        collapseButton.setOnAction(event -> collapseDegree());
        writeExpandedLabel();
    }

    public void setAppController(AppController controller) {
        this.mainController = controller;
    }

    private void handleFileSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select .XML Program File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (selectedFile != null) {
            fileLoadedLabel.setOpacity(1.0);
        }
        mainController.processFile(selectedFile.getPath());
    }

    public void setProgram(String programName, int maxDegree) {
        this.expandedDegree = 0;
        this.maxDegree = maxDegree;
        writeExpandedLabel();

        selectProgram.setValue(programName);
        selectProgram.getItems().clear();
        selectProgram.getItems().add(programName);
    }

    private void expandDegree() {
        if (expandedDegree < maxDegree) {
            expandedDegree++;
            writeExpandedLabel();
        }
    }

    private void collapseDegree() {
        if (expandedDegree > 0) {
            expandedDegree--;
            writeExpandedLabel();
        }
    }

    public void writeExpandedLabel() {
        degreeLabel.setText(expandedDegree + "/" + maxDegree);
        if (expandedDegree == maxDegree) {
            expandButton.setDisable(true);
        } else {
            expandButton.setDisable(false);
        }

        if (expandedDegree == 0) {
            collapseButton.setDisable(true);
        } else {
            collapseButton.setDisable(false);
        }
    }
}
