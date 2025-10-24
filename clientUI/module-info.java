module clientUI {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;
    requires okhttp3;
    requires annotations;
    requires okio;
    requires webApp;
    requires gson;


    opens client.util to javafx.fxml;
    opens client.components.body to javafx.fxml;
    opens client.components.runHistory to javafx.fxml;
    opens client.components.programTab to javafx.fxml;
    opens client.components.header to javafx.fxml;
    opens client.components.instructionHistory to javafx.fxml;
    opens client.components.runMenu to javafx.fxml;
    opens client.components.expandWindow to javafx.fxml;
    opens client.components.availableUsers to javafx.fxml;
    opens client.components.dashboardBody to javafx.fxml;
    opens client.components.login to javafx.fxml;

    exports client.util to javafx.graphics, javafx.fxml;
    exports client.components.programTab to javafx.graphics, javafx.fxml;
    exports client.components.header to javafx.graphics, javafx.fxml;
    exports client.components.runHistory to javafx.graphics, javafx.fxml;
    exports client.components.body to engine, javafx.fxml, javafx.graphics;
    exports client.components.instructionHistory to javafx.fxml, javafx.graphics;
    exports client.components.runMenu to javafx.fxml, javafx.graphics;
    exports client.components.expandWindow to javafx.fxml, javafx.graphics;
    exports client.components.availableUsers to javafx.fxml, javafx.graphics;
    exports client.components.dashboardBody to javafx.fxml, javafx.graphics;
    exports client.components.login to javafx.fxml, javafx.graphics;

}