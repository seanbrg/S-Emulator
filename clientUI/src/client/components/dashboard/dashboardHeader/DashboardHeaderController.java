package client.components.dashboard.dashboardHeader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

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

    private static final String PROGRAMS_SERVLET_URL = "http://localhost:8080/semulator/programs";

    private int availableCredits = 50;

    // Callback for notifying when a program is uploaded
    private Runnable onProgramUploadedCallback;

    @FXML
    public void initialize() {
        loadFileButton.setOnAction(event -> onLoadFileClicked());
        chargeCreditsButton.setOnAction(event -> onChargeCreditsClicked());
        updateCreditLabel();
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

            availableCredits += amount;
            updateCreditLabel();
            creditsTextField.clear();
            showAlert("Credits Added", "Successfully added " + amount + " credits!");

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number.");
        }
    }

    private void onLoadFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Program File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePath.setText(selectedFile.getAbsolutePath());
            activateProgram(selectedFile);
        } else {
            filePath.setText("No file selected");
        }
    }

    private void activateProgram(File programFile) {
        int programCost = 10;
        uploadProgramFile(programFile, programCost);
    }

    private void uploadProgramFile(File file, int programCost) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        Request request = new Request.Builder()
                .url(PROGRAMS_SERVLET_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showAlert("Upload Failed", "Upload failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {




                        if (onProgramUploadedCallback != null) {
                            onProgramUploadedCallback.run();
                        }
                    });
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Platform.runLater(() -> showAlert("Upload Failed",
                            "Server responded with code: " + response.code() + "\n" + errorMessage));
                }
            }
        });
    }

    private void deductCredits(int amount) {
        //to do
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
        Platform.runLater(() -> userNameLable.setText(userName));
    }


    public void setOnProgramUploadedCallback(Runnable callback) {
        this.onProgramUploadedCallback = callback;
    }
}