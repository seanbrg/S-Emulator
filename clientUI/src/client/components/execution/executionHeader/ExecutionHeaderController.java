package client.components.execution.executionHeader;

import client.components.execution.programTab.ProgramTabController;
import client.util.HttpUtils;
import emulator.utils.WebConstants;
import javafx.application.Platform;
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
import okhttp3.RequestBody;

public class ExecutionHeaderController {
    @FXML public TextField creditsTextField;
    @FXML public Label usernameLabel;
    @FXML private ExecutionStageController mainController;
    @FXML private MenuItem menuItemExpand;
    @FXML private MenuItem menuItemThemeLight;
    @FXML private MenuItem menuItemThemeDark;
    @FXML private ProgressBar progressBar;
    @FXML public Menu menuViewLabels;
    @FXML public Menu menuViewVariables;
    @FXML public MenuItem menuViewClear;
    @FXML public Label creditsLabel;
    @FXML private Button loadCreditsButton;
    @FXML private Button backToDashboard;

    private Scene scene;
    private ToggleGroup labelsGroup;
    private ToggleGroup varsGroup;
    private int availableCredits;
    private String currentUsername;

    @FXML
    private void initialize() {
        this.labelsGroup = new ToggleGroup();
        this.varsGroup = new ToggleGroup();

        menuItemExpand.setOnAction(event -> handleExpand());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());
        loadCreditsButton.setOnAction(event -> onChargeCreditsClicked());
        backToDashboard.setOnAction(event -> handleBackToDashboard());

        progressBar.setProgress(0);
        progressBar.setVisible(false);
        creditsTextField.setFocusTraversable(false);
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

    private void onChargeCreditsClicked() {
        String text = creditsTextField.getText().trim();
        if (text.isEmpty()) {
            HttpUtils.showAlert("Missing Input", "Please enter how many credits to add.", scene);
            return;
        }

        try {
            int amount = Integer.parseInt(text);
            if (amount <= 0) {
                HttpUtils.showAlert("Invalid Amount", "Please enter a positive number.", scene);
                return;
            }

            // Send credits to server
            loadCreditsButton.setDisable(true);

            String creditsUrl = WebConstants.USERS_URL + "?username=" + currentUsername + "&credits=" + amount;

            HttpUtils.postAsync(creditsUrl, RequestBody.create(new byte[0]))
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            availableCredits += amount;
                            updateCreditLabel();
                            creditsTextField.clear();
                            loadCreditsButton.setDisable(false);
                            HttpUtils.showAlert("Credits Added",
                                    "Successfully added " + amount + " credits!", scene);
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            loadCreditsButton.setDisable(false);
                            HttpUtils.showAlert("Error",
                                    "Failed to add credits: " + ex.getMessage(), scene);
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            HttpUtils.showAlert("Invalid Input", "Please enter a valid number.", scene);
        }
    }

    private void postAddCredits(int credits) {

    }


    private void updateCreditLabel() {
        creditsLabel.setText(String.valueOf(availableCredits));
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

    public void setUserName(String currentUserName) {
        this.currentUsername = currentUserName;
        usernameLabel.setText(currentUserName);

        String creditsUrl = WebConstants.USERS_URL + "?username=" + currentUsername + "&credits=" + 0;
        HttpUtils.postAsync(creditsUrl, RequestBody.create(new byte[0])).thenAccept(response -> {
            Platform.runLater(() -> {
                System.out.println(response);
                availableCredits = Integer.parseInt(response.trim());
                updateCreditLabel();
                creditsTextField.clear();
                loadCreditsButton.setDisable(false);
            });
        });
    }
    private void handleBackToDashboard() {
        mainController.backToDashboard();
    }
    public int getAvailableCredits() {
        return availableCredits;
    }

    public void deductCredits(int amount) {
        availableCredits -= amount;
        updateCreditLabel();

        // Sync with server
        String creditsUrl = WebConstants.USERS_URL + "?username=" + currentUsername + "&credits=" + (-amount);
        HttpUtils.postAsync(creditsUrl, RequestBody.create(new byte[0])).thenAccept(response -> {
            Platform.runLater(() -> {
                System.out.println("Credits deducted: " + amount);
            });
        });
    }
}


