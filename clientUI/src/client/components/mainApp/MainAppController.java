package client.components.mainApp;

import client.components.dashboard.dashboardStage.DashboardStageController;

import client.components.execution.executionStage.ExecutionStageController;
import client.components.login.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static client.util.Constants.DASHBOARD_FXML;
import static client.util.Constants.LOGIN_FXML;

public class MainAppController {

    private final Stage primaryStage;

    private Parent loginComponent;
    private Parent executionComponent;
    private LoginController loginController;

    private Parent dashboardComponent;
    private DashboardStageController dashboardStageController;
    private ExecutionStageController executionStageController;
    private Scene scene;

    private String currentUserName;

    public MainAppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // ---------------------- Initialization ----------------------

    public void showLogin() {
        try {
            if (loginComponent == null) {
                URL fxmlUrl = getClass().getResource(LOGIN_FXML);
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                loginComponent = fxmlLoader.load();
                loginController = fxmlLoader.getController();
                loginController.setMainAppController(this);
            }

            this.scene = new Scene(loginComponent);
            String css = getClass().getResource("/resources/styles/style-dark.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/icon.png")));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboard() {
        try {
            if (dashboardComponent == null) {
                URL fxmlUrl = getClass().getResource(DASHBOARD_FXML);
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                dashboardComponent = fxmlLoader.load();
                dashboardStageController = fxmlLoader.getController();
                dashboardStageController.setMainAppController(this);
                Platform.runLater(() -> {
                    dashboardStageController.setScene(scene);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadExecution() {
        try {
            URL fxmlUrl = getClass().getResource("/client/components/execution/executionStage/ExecutionStage.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            executionComponent = fxmlLoader.load();
            executionStageController = fxmlLoader.getController();
            executionStageController.setMainAppController(this);
            Platform.runLater(() -> {
                executionStageController.setScene(scene);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------- Stage switching ----------------------

    public void switchToDashboard(String userName) {
        this.currentUserName = userName;
        loadDashboard();

        Platform.runLater(() -> {
            Scene currentScene = primaryStage.getScene();
            currentScene.setRoot(dashboardComponent);
            dashboardStageController.setScene(currentScene);
            dashboardStageController.setActive(userName);
            primaryStage.sizeToScene();
        });
    }


    public void switchToExecute(List<String> programNames) {
        loadExecution();

        Platform.runLater(() -> {
            Scene currentScene = primaryStage.getScene();
            currentScene.setRoot(executionComponent);
            executionStageController.setScene(currentScene);
            executionStageController.setActive(programNames, currentUserName);
            primaryStage.sizeToScene();
        });
    }



    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName = null;
            primaryStage.getScene().setRoot(loginComponent);
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public String getCurrentUserName() {
        return currentUserName;
    }

}

