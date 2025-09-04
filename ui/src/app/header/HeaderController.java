package app.header;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;

public class HeaderController {
    @FXML
    private Button loadFileButton;

    @FXML
    private Label fileLoadedLabel;

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> handleFileSelection());
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
    }
}
