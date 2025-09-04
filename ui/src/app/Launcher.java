package app;

import app.header.HeaderController;
import execute.Engine;
import execute.EngineImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppController appController = new AppController();
        Engine engine = new EngineImpl();

        appController.setEngine(engine);

        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource("/app/header/Header.fxml");
        loader.setLocation(url);
        Parent root = loader.load(url.openStream());
        HeaderController headerController = loader.getController();
        appController.setHeaderController(headerController);

        Scene scene = new Scene(root);

        primaryStage.setTitle("S-Emulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        launch(args);
    }
}