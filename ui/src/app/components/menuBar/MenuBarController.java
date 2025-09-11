package app.components.menuBar;

import app.components.body.AppController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class MenuBarController {
    @FXML private AppController mainController;

    @FXML private Menu menuRun;
    @FXML private Menu menuDebug;

    @FXML public Label loadLabel;
    @FXML private MenuItem menuItemLoad;
    @FXML private MenuItem menuItemFindComponent;
    @FXML private MenuItem menuItemExpand;
    @FXML private MenuItem menuItemThemeLight;
    @FXML private MenuItem menuItemThemeDark;

    @FXML private ProgressBar progressBar;

    private Scene scene;

    @FXML
    private void initialize() {
        //menuRun.setOnAction(e -> handleRun());
        //menuDebug.setOnAction(e -> handleDebug());

        menuItemLoad.setOnAction(event -> handleLoad());
        menuItemFindComponent.setOnAction(event -> handleFindComponent());
        menuItemExpand.setOnAction(event -> handleExpand());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());
    }

    public void setMainController(AppController mainController) { this.mainController = mainController; }

    public void setScene(Scene scene) { this.scene = scene; }

    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Program File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(scene.getWindow());
        if (selectedFile != null) mainController.processFile(selectedFile.getPath());
    }

    private void handleFindComponent() {
        // TODO: Implement find component functionality
    }


    private void handleThemeLight() {
        mainController.switchTheme(false);
    }

    private void handleThemeDark() {
        mainController.switchTheme(true);
    }

    private void handleExpand() {
        //TODO: handle expand through menu
    }
}
