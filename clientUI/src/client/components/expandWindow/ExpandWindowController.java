package src.client.components.expandWindow;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ExpandWindowController {
    @FXML public Button expandButton;
    @FXML public Spinner degreeSpinner;
    @FXML public TextField maxDegreeConsole;

    private int maxDegree;
    private int result;

    @FXML
    public void initialize() {
        result = 0;
        maxDegree = 0;

        expandButton.disableProperty().bind(degreeSpinner.valueProperty().isNull());
        maxDegreeConsole.setAlignment(Pos.CENTER);

        degreeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxDegree, maxDegree)
        );
        degreeSpinner.getEditor().setTextFormatter(new TextFormatter<String>(
                c -> c.getControlNewText().matches("\\d*") ? c : null
        ));
        this.expandButton.setOnAction(e -> handleExpand());

    }

    public int getResult() { return result; }

    private void handleExpand() {
        result = (int) degreeSpinner.getValue();
        Stage stage = (Stage) expandButton.getScene().getWindow();
        stage.close();
    }

    public void setMaxExpansion(int maxExpansion) {
        this.maxDegree = maxExpansion;

        javafx.application.Platform.runLater(() -> {
            maxDegreeConsole.clear();
            maxDegreeConsole.setText(String.valueOf(maxDegree));
        });


        degreeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxDegree, 0)
        );
    }
}
