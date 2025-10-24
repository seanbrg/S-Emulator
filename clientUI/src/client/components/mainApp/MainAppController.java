package client.components.mainApp;

import client.components.dashboard.DashboardController;

import client.components.login.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static client.util.Constants.DASHBOARD_FXML;
import static client.util.Constants.LOGIN_FXML;

public class MainAppController {

    private final Stage primaryStage;

    private Parent loginComponent;
    private LoginController loginController;

    private Parent dashboardComponent;
    private DashboardController dashboardController;

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

            Scene scene = new Scene(loginComponent);
            primaryStage.setScene(scene);
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
                dashboardController = fxmlLoader.getController();
                dashboardController.setMainAppController(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------- Stage switching ----------------------

    public void switchToDashboard(String userName) {
        this.currentUserName = userName;
        loadDashboard();

        Platform.runLater(() -> {
            dashboardController.updateHeader("Welcome, " + currentUserName + "!");
            primaryStage.getScene().setRoot(dashboardComponent);
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
}

