package client.components.dashboard.dashboardHeader;

import client.components.dashboard.dashboardStage.DashboardStageController;
import client.util.HttpUtils;
import emulator.utils.WebConstants;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DashboardHeaderController {

    @FXML private MenuItem menuItemLoad;
    @FXML private MenuItem menuItemThemeLight;
    @FXML private MenuItem menuItemThemeDark;
    @FXML private Button loadCreditsButton;
    @FXML private Label usernameLabel;
    @FXML private Label creditsLabel;
    @FXML private TextField creditsTextField;
    @FXML private ProgressBar progressBar;

    private int availableCredits = 50; // starting amount
    private String currentUsername = "";
    private DashboardStageController dashboardController;

    // Callback for notifying when a program is uploaded
    private Runnable onProgramUploadedCallback;
    private Scene scene;

    @FXML
    public void initialize() {
        menuItemLoad.setOnAction(event -> handleLoad());
        menuItemThemeLight.setOnAction(event -> handleThemeLight());
        menuItemThemeDark.setOnAction(event -> handleThemeDark());
        loadCreditsButton.setOnAction(event -> onChargeCreditsClicked());
        updateCreditLabel();

        progressBar.setProgress(0);
        progressBar.setVisible(false);
        Platform.runLater(() -> progressBar.requestFocus());
        creditsTextField.setFocusTraversable(false);
    }

    public void setDashboardController(DashboardStageController controller) {
        this.dashboardController = controller;
    }

    private void handleThemeLight() {
        switchTheme(false);
    }

    private void handleThemeDark() {
        switchTheme(true);
    }

    private void switchTheme(boolean dark) {
        if (scene == null) return;

        String cssPath = dark ?
                "/resources/styles/style-dark.css" :
                "/resources/styles/style-light.css";

        URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            String cssUrl = resource.toExternalForm();

            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl);
            scene.getRoot().applyCss();
        }
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

            String url = WebConstants.USERS_URL + "?username=" + currentUsername + "&credits=" + amount;

            HttpUtils.postAsync(url, RequestBody.create(new byte[0]))
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

    @FXML
    private void handleLoad() {
        File file = chooseXmlFile();
        if (file == null) return;
        final String newPath = file.getPath();

        progressBar.setProgress(0);
        progressBar.setVisible(true);

        // start continuous progress
        Timeline spinner = startContinuousProgress();

        // background load
        Task<List<String>> task = dashboardController.createLoadTask(newPath);

        task.setOnSucceeded(e -> {
            finish(true, task.getValue(), spinner);
            // Trigger callback to refresh programs list
            if (onProgramUploadedCallback != null) {
                onProgramUploadedCallback.run();
            }
        });
        task.setOnFailed(e -> finish(false, null, spinner));

        new Thread(task, "load-xml").start();
    }

    private File chooseXmlFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Program File");
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        return fc.showOpenDialog(scene.getWindow());
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

    private void finish(boolean ok, List<String> funcNames, Timeline spinner) {
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
                if (!ok) {
                    dashboardController.alertLoadFailed();
                }
                progressBar.setVisible(false);
            });
            stall.play();
        });

        toFull.play();
    }

    private void updateCreditLabel() {
        Platform.runLater(() -> creditsLabel.setText(String.valueOf(availableCredits)));
    }

    public void setUserName(String userName) {
        this.currentUsername = userName;
        Platform.runLater(() -> usernameLabel.setText(userName));
    }

    public void setOnProgramUploadedCallback(Runnable callback) {
        this.onProgramUploadedCallback = callback;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}