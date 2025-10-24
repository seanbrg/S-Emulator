package client.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.net.URL;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource("/client/components/login/Login.fxml"); // login first
        loader.setLocation(url);
        Parent root = loader.load(url.openStream());

        Scene scene = new Scene(root);
        String css = getClass().getResource("/client/resources/styles/style-dark.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("S-Emulator");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/client/resources/images/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pass stage to LoginController
        src.client.components.login.LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }
}
