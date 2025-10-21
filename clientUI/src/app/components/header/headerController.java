package app.components.header;

import app.components.body.AppController;
import app.components.programTab.ProgramTabController;
import execute.dto.InstructionDTO;
import execute.dto.VariableDTO;
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
import java.util.List;

import javafx.animation.*;

public class headerController {
    @FXML private AppController mainController;

    @FXML public Label loadLabel;
    @FXML private MenuItem menuItemLoad;
    @FXML private MenuItem menuItemExpand;
    @FXML private MenuItem menuItemThemeLight;
    @FXML private MenuItem menuItemThemeDark;
    @FXML private ProgressBar progressBar;
    @FXML public Menu menuViewLabels;
    @FXML public Menu menuViewVariables;
    @FXML public MenuItem menuViewClear;

    private Scene scene;
    private ToggleGroup labelsGroup;
    private ToggleGroup varsGroup;

    @FXML
    private void initialize() {
        this.labelsGroup = new ToggleGroup();
        this.varsGroup = new ToggleGroup();

        menuItemLoad.setOnAction(event -> handleLoad());
        menuItemExpand.setOnAction(event -> handleExpand());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());

        loadLabel.setText("No file loaded.");  // initial state
        progressBar.setProgress(0);
        progressBar.setVisible(false);

    }

    public void setMainController(AppController mainController) {
        this.mainController = mainController;
        javafx.application.Platform.runLater(this::enableDynamicViewMenus);
    }

    public void setScene(Scene scene) { this.scene = scene; }

    @FXML
    private void handleLoad() {
        File file = chooseXmlFile();
        if (file == null) return;

        final String newPath = file.getPath();
        final String oldPath = loadLabel.getText();
        loadLabel.setText("Loading file...");
        progressBar.setProgress(0);
        progressBar.setVisible(true);

        // start continuous progress
        Timeline spinner = startContinuousProgress();

        // background load (no UI work inside)
        Task<List<String>> task = mainController.createLoadTask(newPath);

        // when task leaves RUNNING, finish the bar and then open/notify
        task.setOnSucceeded(e -> finish(true, task.getValue(), newPath, spinner));
        task.setOnFailed(e -> finish(false, null, oldPath, spinner));

        new Thread(task, "load-xml").start();
    }

    private void finish(boolean ok, List<String> funcNames, String path, Timeline spinner) {
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
                loadLabel.setText(path);
                if (ok) {
                    mainController.newProgram(funcNames);
                }
                else {
                    mainController.alertLoadFailed();
                }
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

    private void enableDynamicViewMenus() {
        clearMenus();

        // Rebuild whenever selected tab changes
        mainController.currentTabControllerProperty().addListener((obs, oldTab, newTab) -> rebuildMenus(newTab));
        // Initial build (works even if there are no tabs yet)
        rebuildMenus(mainController.currentTabControllerProperty().get());
    }

    private void rebuildMenus(ProgramTabController tab) {
        // If no tab/table/items → show empty menus, no NPEs
        if (tab == null || tab.getInstructionsTable() == null || tab.getInstructionsTable().getItems() == null) {
            clearMenus();
            return;
        }

        var items = tab.getInstructionsTable().getItems();

        // Collect distinct labels/vars from current table
        var labels = new java.util.LinkedHashSet<String>();
        var vars   = new java.util.LinkedHashSet<String>();
        items.forEach(dto -> {
            if (dto.getSelfLabel() != null && !dto.getSelfLabel().getLabel().isBlank()) labels.add(dto.getSelfLabel().getLabel());
            if (dto.getVariables() != null) dto.getVariables().forEach(v -> {
                if (v != null && v.getName() != null && !v.getName().isBlank()) vars.add(v.getName());
            });
        });

        // Build menus (with Clear)
        menuViewLabels.getItems().setAll(buildMenu(labels, lbl -> highlightByLabel(tab, lbl)));
        menuViewVariables.getItems().setAll(buildMenu(vars, vn -> highlightByVar(tab, vn)));

        // If the list instance is swapped later, rebuild again
        tab.getInstructionsTable().itemsProperty().addListener((o, ov, nv) -> rebuildMenus(tab));
    }

    private List<MenuItem> buildMenu(java.util.Set<String> entries, java.util.function.Consumer<String> onPick) {
        var list = new java.util.ArrayList<MenuItem>();
        var clear = new RadioMenuItem("None");
        var group = new ToggleGroup(); clear.setToggleGroup(group); clear.setSelected(true);
        clear.setOnAction(e -> mainController.clearHighlights());
        list.add(clear);
        for (String s : entries) {
            var mi = new RadioMenuItem(s);
            mi.setToggleGroup(group);
            mi.setOnAction(e -> onPick.accept(s));
            list.add(mi);
        }
        return list;
    }

    private void highlightByLabel(ProgramTabController tab, String label) {
        var lines = new java.util.ArrayList<Integer>();
        tab.getInstructionsTable().getItems().forEach(dto -> {
            if (label.equals(dto.getSelfLabel().getLabel())) lines.add(dto.getNum());
        });
        mainController.replaceHighlights(lines);
    }

    private void highlightByVar(ProgramTabController tab, String varName) {
        var lines = new java.util.ArrayList<Integer>();
        tab.getInstructionsTable().getItems().forEach(dto -> {
            if (dto.getVariables() != null && dto.getVariables().stream().anyMatch(v -> v != null && varName.equals(v.getName())))
                lines.add(dto.getNum());
        });
        mainController.replaceHighlights(lines);
    }

    private void clearMenus() {
        menuViewLabels.getItems().clear();
        menuViewVariables.getItems().clear();
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
