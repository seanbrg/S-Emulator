module engine {
    requires javafx.swt;
    requires javafx.base;
    requires java.xml;
    requires gson;

    exports execute to offlineUI, clientUI;
    exports execute.dto;
    exports logic.instructions to offlineUI;
}