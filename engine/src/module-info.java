module engine {
    requires java.xml;

    exports execute to offlineUI, clientUI;
    exports execute.dto;
    exports logic.instructions to offlineUI;
}