module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;

    opens app to javafx.fxml, engine;
    opens app.components.body to javafx.fxml, engine;
    opens app.components.runHistory to javafx.fxml, engine;
    opens app.components.programTab to javafx.fxml;
    opens app.components.menuBar to javafx.fxml;
    opens app.components.instructionHistory to javafx.fxml;
    opens app.components.runMenu to javafx.fxml;

    exports app to javafx.graphics, javafx.fxml;
    exports app.components.programTab to javafx.graphics, javafx.fxml;
    exports app.components.menuBar to javafx.graphics, javafx.fxml;
    exports app.components.runHistory to javafx.graphics, javafx.fxml;
    exports app.components.body to engine, javafx.fxml, javafx.graphics;
    exports app.components.instructionHistory to javafx.fxml, javafx.graphics;
    exports app.components.runMenu to javafx.fxml, javafx.graphics;
}