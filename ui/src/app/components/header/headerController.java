package app.components.header;

import app.components.body.AppController;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.io.File;
import javafx.animation.*;

public class headerController {
    @FXML private AppController mainController;

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
        menuItemLoad.setOnAction(event -> handleLoad());
        menuItemFindComponent.setOnAction(event -> handleFindComponent());
        menuItemExpand.setOnAction(event -> handleExpand());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());

        loadLabel.setText("No file loaded.");  // initial state
        progressBar.setProgress(0);
        progressBar.setVisible(false);

    }

    public void setMainController(AppController mainController) { this.mainController = mainController; }

    public void setScene(Scene scene) { this.scene = scene; }

    @FXML
    private void handleLoad() {
        File file = chooseXmlFile();
        if (file == null) return;

        final String path = file.getPath();
        loadLabel.setText("Loading file...");
        progressBar.setProgress(0);
        progressBar.setVisible(true);

        // start continuous progress
        Timeline spinner = startContinuousProgress();

        // background load (no UI work inside)
        Task<String> task = mainController.createLoadTask(path);

        // when task leaves RUNNING, finish the bar and then open/notify
        task.setOnSucceeded(e -> finish(true, task.getValue(), 0, path, spinner));
        task.setOnFailed(e -> finish(false, null, 0, path, spinner));

        new Thread(task, "load-xml").start();
    }

    private void finish(boolean ok, String programName, int degree, String path, Timeline spinner) {
        spinner.stop();
        var toFull = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), Math.min(progressBar.getProgress(), 0.995))),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(progressBar.progressProperty(), 1.0))
        );

        toFull.setOnFinished(ev -> {
            // stall 0.2s after reaching 100%
            var stall = new PauseTransition(Duration.seconds(0.2));
            stall.setOnFinished(__ -> {
                loadLabel.setText(ok ? path : "Failed to load: " + path);
                if (ok) mainController.newProgram(programName, degree);
                progressBar.setVisible(false);
            });
            stall.play();
        });

        toFull.play();
    }

    private Timeline startContinuousProgress() {
        Timeline t = new Timeline(
                new KeyFrame(Duration.millis(40), e -> {
                    double p = progressBar.getProgress();
                    progressBar.setProgress(p >= 0.995 ? 0.995 : p + 0.003); // smooth, continuous
                })
        );
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
        return t;
    }

    private File chooseXmlFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Program File");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        return fc.showOpenDialog(scene.getWindow());
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
        mainController.expandProgram();
    }
}
