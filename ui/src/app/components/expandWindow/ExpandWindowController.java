package app.components.expandWindow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

public class ExpandWindowController {
    @FXML public Button expandButton;
    @FXML public ChoiceBox<Integer> expandChoiceBox;

    @FXML
    public void initialize() {
        this.expandButton.setOnAction(e -> handleExpand());

    }

    private void handleExpand() {
    }
}
