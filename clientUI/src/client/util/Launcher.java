package client.util;

import client.components.mainApp.MainAppController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("S-Emulator");

        try (InputStream iconStream = getClass().getResourceAsStream("/client/resources/images/icon.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Icon not found or failed to load.");
        }

        MainAppController mainController = new MainAppController(primaryStage);
        mainController.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
