package app;

import app.components.body.AppController;
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
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/app/components/body/body.fxml");
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());
        AppController controller = fxmlLoader.getController();

        Scene scene = new Scene(root);
        controller.setScene(scene);
        String css = getClass().getResource("/app/resources/styles/style-dark.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("S-Emulator");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/app/resources/images/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }
}