package client.components.dashboard.dashboardHeader;

import client.components.dashboard.dashboardStage.DashboardStageController;
import client.util.HttpUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DashboardHeaderController {

    @FXML
    private Button loadFileButton;
    @FXML
    private Button chargeCreditsButton;
    @FXML
    private Label filePath;
    @FXML
    private Label userNameLable;
    @FXML
    private Label availableCreditsLable;
    @FXML
    private TextField creditsTextField;
    @FXML
    private ProgressBar progressBar;

    private static final String PROGRAMS_SERVLET_URL = "http://localhost:8080/semulator/programs";
    private static final String USERS_SERVLET_URL = "http://localhost:8080/semulator/users";

    private int availableCredits = 50;
    private String currentUsername = "";
    private DashboardStageController dashboardController;

    // Callback for notifying when a program is uploaded
    private Runnable onProgramUploadedCallback;
    private Scene scene;

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> handleLoad());
        chargeCreditsButton.setOnAction(event -> onChargeCreditsClicked());
        updateCreditLabel();
    }

    public void setDashboardController(DashboardStageController controller) {
        this.dashboardController = controller;
    }

    private void onChargeCreditsClicked() {
        String text = creditsTextField.getText().trim();
        if (text.isEmpty()) {
            showAlert("Missing Input", "Please enter how many credits to add.");
            return;
        }

        try {
            int amount = Integer.parseInt(text);
            if (amount <= 0) {
                showAlert("Invalid Amount", "Please enter a positive number.");
                return;
            }

            // Send credits to server
            chargeCreditsButton.setDisable(true);

            String url = USERS_SERVLET_URL + "?username=" + currentUsername + "&credits=" + amount;

            HttpUtils.postAsync(url, RequestBody.create(new byte[0]))
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            availableCredits += amount;
                            updateCreditLabel();
                            creditsTextField.clear();
                            chargeCreditsButton.setDisable(false);
                            showAlert("Credits Added", "Successfully added " + amount + " credits!");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            chargeCreditsButton.setDisable(false);
                            showAlert("Error", "Failed to add credits: " + ex.getMessage());
                        });
                        return null;
                    });

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number.");
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

        // background load (no UI work inside)
        Task<List<String>> task = dashboardController.createLoadTask(newPath);

        // when task leaves RUNNING, finish the bar and then open/notify
        task.setOnSucceeded(e -> {
            finish(true, task.getValue(), spinner);
            // Trigger callback to refresh programs list
            if (onProgramUploadedCallback != null) {
                onProgramUploadedCallback.run();
            }
        });
        task.setOnFailed(e -> finish(false, null, spinner));

        new Thread(task, "load-xml").start();

        Platform.runLater(() -> filePath.setText(newPath));
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
        Platform.runLater(() -> availableCreditsLable.setText(String.valueOf(availableCredits)));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUserName(String userName) {
        this.currentUsername = userName;
        Platform.runLater(() -> userNameLable.setText(userName));
    }

    public void setOnProgramUploadedCallback(Runnable callback) {
        this.onProgramUploadedCallback = callback;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}