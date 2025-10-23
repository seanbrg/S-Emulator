module clientUI {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;
    requires okhttp3;
    requires annotations;
    requires okio;
    requires webApp;
    requires gson;


    opens src.client.util to javafx.fxml;
    opens src.client.components.body to javafx.fxml;
    opens src.client.components.runHistory to javafx.fxml;
    opens src.client.components.programTab to javafx.fxml;
    opens src.client.components.header to javafx.fxml;
    opens src.client.components.instructionHistory to javafx.fxml;
    opens src.client.components.runMenu to javafx.fxml;
    opens src.client.components.expandWindow to javafx.fxml;

    exports src.client.util to javafx.graphics, javafx.fxml;
    exports src.client.components.programTab to javafx.graphics, javafx.fxml;
    exports src.client.components.header to javafx.graphics, javafx.fxml;
    exports src.client.components.runHistory to javafx.graphics, javafx.fxml;
    exports src.client.components.body to engine, javafx.fxml, javafx.graphics;
    exports src.client.components.instructionHistory to javafx.fxml, javafx.graphics;
    exports src.client.components.runMenu to javafx.fxml, javafx.graphics;
    exports src.client.components.expandWindow to javafx.fxml, javafx.graphics;
}