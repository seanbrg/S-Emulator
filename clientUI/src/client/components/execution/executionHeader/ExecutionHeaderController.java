package client.components.execution.executionHeader;

import client.components.execution.programTab.ProgramTabController;
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
import client.components.execution.executionStage.ExecutionStageController;

public class ExecutionHeaderController {
    @FXML
    private ExecutionStageController mainController;

    @FXML
    private MenuItem menuItemExpand;
    @FXML
    private MenuItem menuItemThemeLight;
    @FXML
    private MenuItem menuItemThemeDark;
    @FXML
    private ProgressBar progressBar;
    @FXML
    public Menu menuViewLabels;
    @FXML
    public Menu menuViewVariables;
    @FXML
    public MenuItem menuViewClear;

    private Scene scene;
    private ToggleGroup labelsGroup;
    private ToggleGroup varsGroup;

    @FXML
    private void initialize() {
        this.labelsGroup = new ToggleGroup();
        this.varsGroup = new ToggleGroup();

        menuItemExpand.setOnAction(event -> handleExpand());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());

        progressBar.setProgress(0);
        progressBar.setVisible(false);

    }

    public void setMainController(ExecutionStageController mainController) {
        this.mainController = mainController;
        javafx.application.Platform.runLater(this::enableDynamicViewMenus);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
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
        var vars = new java.util.LinkedHashSet<String>();
        items.forEach(dto -> {
            if (dto.getSelfLabel() != null && !dto.getSelfLabel().getLabel().isBlank())
                labels.add(dto.getSelfLabel().getLabel());
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
        var group = new ToggleGroup();
        clear.setToggleGroup(group);
        clear.setSelected(true);
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


