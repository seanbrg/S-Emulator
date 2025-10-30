package client.components.dashboard.dashboardStage;

import client.components.dashboard.availableFunctions.AvailableFunctionsController;
import client.components.dashboard.availableUsers.AvailableUsersController;
import client.components.dashboard.availablePrograms.AvailableProgramsController;
import client.components.dashboard.dashboardHeader.DashboardHeaderController;
import client.components.dashboard.userHistory.UserHistoryController;
import client.components.mainApp.MainAppController;
import client.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import emulator.utils.WebConstants;
import execute.dto.VariableDTO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class DashboardStageController {

    @FXML private HBox dashboardHeader;
    @FXML private DashboardHeaderController dashboardHeaderController;

    @FXML private BorderPane availableUsers;
    @FXML private AvailableUsersController availableUsersController;

    @FXML private BorderPane availablePrograms;
    @FXML private AvailableProgramsController availableProgramsController;

    @FXML private BorderPane availableFunctions;
    @FXML private AvailableFunctionsController availableFunctionsController;

    @FXML private BorderPane userHistory;
    @FXML private UserHistoryController userHistoryController;

    private MainAppController mainAppController;
    private static final Gson GSON = new Gson();
    private Scene scene;

    @FXML
    public void initialize() {
        // Set up callback to refresh programs table when a new program is uploaded
        if (dashboardHeaderController != null) {
            dashboardHeaderController.setDashboardController(this);
            dashboardHeaderController.setOnProgramUploadedCallback(() -> {
                // Force immediate refresh of programs table
                // availableProgramsController.startListRefresher();
            });
        }

        // Set up HTTP status update for users controller
        if (availableUsersController != null) {
            availableUsersController.setHttpStatusUpdate(this::updateHttpStatus);
        }

        // Bind user history to selected user
        if (userHistoryController != null && availableUsersController != null) {
            userHistoryController.selectedUsernameProperty().bind(
                    availableUsersController.selectedUsernameProperty()
            );
            userHistoryController.setDashboardStageController(this);
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        if (this.dashboardHeaderController != null) {
            this.dashboardHeaderController.setScene(scene);
        }
    }

    public void setMainAppController(MainAppController controller) {
        this.mainAppController = controller;
        if (availableFunctionsController != null)
            availableFunctionsController.setMainDashboardController(this);
        if (availableProgramsController != null)
            availableProgramsController.setMainDashboardController(this);
    }

    @FXML
    private void handleLogout() {
        // Stop users refresher
        if (availableUsersController != null) {
            availableUsersController.close();
        }

        // Stop programs refresher
        if (availableProgramsController != null) {
            availableProgramsController.stopListRefresher();
        }

        // Stop functions refresher
        if (availableFunctionsController != null) {
            availableFunctionsController.stopListRefresher();
        }

        // Stop user history refresher
        if (userHistoryController != null) {
            userHistoryController.stopRefresher();
        }

        if (mainAppController != null) {
            mainAppController.switchToLogin();
        }
        // Bind user history to selected user
        if (userHistoryController != null && availableUsersController != null) {
            userHistoryController.selectedUsernameProperty().bind(
                    availableUsersController.selectedUsernameProperty()
            );
        }
    }

    public void setActive(String userName) {
        // Start users refresher
        if (availableUsersController != null) {
            availableUsersController.startListRefresher();
            availableUsersController.setDashboardStageController(this, userName);
        }

        // Start programs refresher
        if (availableProgramsController != null) {
            availableProgramsController.startListRefresher();
        }

        // Start functions refresher
        if (availableFunctionsController != null) {
            availableFunctionsController.startListRefresher();
        }

        // Start user history refresher and bind user selection
        if (userHistoryController != null) {
            userHistoryController.startListRefresher();
            userHistoryController.setDashboardStageController(this);
            availableFunctionsController.selectedProgramNameProperty()
                    .bind(availableProgramsController.selectedProgramNameProperty());
        }

        // Show username in header and bind credits
        if (dashboardHeaderController != null && availableUsersController != null) {
            dashboardHeaderController.setUserName(userName);
            dashboardHeaderController.setDashboardController(this);

            // Set up the listener to sync credits from users controller to header
            availableUsersController.currentCreditsProperty().addListener((observable, oldValue, newValue) -> {
                dashboardHeaderController.creditsProperty().setValue(newValue);
            });


            Platform.runLater(() -> {
                int currentCredits = availableUsersController.currentCreditsProperty().get();
                dashboardHeaderController.creditsProperty().setValue(currentCredits);


                if (availableProgramsController != null) {
                    availableProgramsController.bindCredits(dashboardHeaderController.creditsProperty());
                }
            });
        }
    }

    private void updateHttpStatus(String status) {

    }

    public Task<List<String>> createLoadTask(String filePath) {
        return new Task<>() {
            @Override
            protected List<String> call() throws IOException {
                String uploadUrl = WebConstants.PROGRAMS_URL;
                String listUrl = WebConstants.PROGRAMS_LIST_URL;

                File file = new File(filePath);
                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/xml");
                    }
                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {
                        try (FileInputStream in = new FileInputStream(file)) {
                            sink.writeAll(Okio.source(in));
                        }
                    }
                };

                try (Response r = HttpUtils.postSync(uploadUrl, requestBody)) {
                    if (!r.isSuccessful()) {
                        throw new IOException("Upload failed: " + r.code());
                    }
                }

                List<String> funcNames;
                try (Response r = HttpUtils.getSync(listUrl)) {
                    if (!r.isSuccessful() || r.body() == null) {
                        throw new IOException("List fetch failed: " + r.code());
                    }
                    String json = r.body().string();
                    funcNames = GSON.fromJson(json, new TypeToken<List<String>>(){}.getType());
                }
                return funcNames;
            }
        };
    }

    public void alertLoadFailed() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Failed");
            alert.setHeaderText("Failed to load program file!");
            alert.setContentText("Please check the file format and try again.");

            // Use main window as owner if available
            if (scene != null && scene.getWindow() != null) {
                alert.initOwner(scene.getWindow());
                // Inherit current theme
                alert.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
            }

            try {
                Stage dlg = (Stage) alert.getDialogPane().getScene().getWindow();
                dlg.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
            } catch (Exception ignored) {  }

            alert.showAndWait();
        });
    }

    public void switchToExecute(List<String> programNames) {
        mainAppController.switchToExecute(programNames);
    }

    public List<String> getDisplayedFuncNames() {
        return availableFunctionsController.getDisplayedFuncNames();
    }
    public void switchToExecuteWithInputs(String programName, List<VariableDTO> inputs, String architecture) {
        List<String> programNames = List.of(programName);
        mainAppController.switchToExecuteWithRerun(programNames, inputs, architecture);
    }
}