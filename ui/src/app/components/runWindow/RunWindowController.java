package app.components.runWindow;

import app.components.body.AppController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RunWindowController {
    @FXML private AppController mainController;

    @FXML private Button buttonRun;
    @FXML private Button buttonDebug;

    @FXML
    public void initialize() {
        buttonRun.setOnAction(event -> handleRun());
        buttonDebug.setOnAction(event -> handleDebug());

        Platform.runLater(() -> {
            buttonRun.disableProperty().bind(
                    mainController.currentTabControllerProperty().isNull()
            );

            buttonDebug.disableProperty().bind(
                    mainController.currentTabControllerProperty().isNull()
            );
        });

    }

    private void handleRun() {
        // TODO: Implement run functionality
    }

    private void handleDebug() {
        // TODO: Implement debug functionality
    }

    private void handleExpand() {
        // TODO: Implement expand functionality
    }


    public void setMainController(AppController appController) {
        this.mainController = appController;
    }
}
