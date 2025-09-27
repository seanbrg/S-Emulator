module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;

    opens app.util to javafx.fxml, engine;
    opens app.components.body to javafx.fxml, engine;
    opens app.components.runHistory to javafx.fxml, engine;
    opens app.components.programTab to javafx.fxml;
    opens app.components.header to javafx.fxml;
    opens app.components.instructionHistory to javafx.fxml;
    opens app.components.runMenu to javafx.fxml;
    opens app.components.expandWindow to javafx.fxml;

    exports app.util to javafx.graphics, javafx.fxml;
    exports app.components.programTab to javafx.graphics, javafx.fxml;
    exports app.components.header to javafx.graphics, javafx.fxml;
    exports app.components.runHistory to javafx.graphics, javafx.fxml;
    exports app.components.body to engine, javafx.fxml, javafx.graphics;
    exports app.components.instructionHistory to javafx.fxml, javafx.graphics;
    exports app.components.runMenu to javafx.fxml, javafx.graphics;
    exports app.components.expandWindow to javafx.fxml, javafx.graphics;
}