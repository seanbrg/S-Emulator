module ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;

    opens app to javafx.fxml, engine;
    opens app.programTab to javafx.fxml;
    opens app.menuBar to javafx.fxml;

    exports app to javafx.graphics, javafx.fxml;
    exports app.programTab to javafx.graphics, javafx.fxml;
    exports app.menuBar to javafx.graphics, javafx.fxml;
    exports app.body to javafx.fxml, javafx.graphics;
    opens app.body to engine, javafx.fxml;
}