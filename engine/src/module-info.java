module engine {
    requires java.xml;

    exports execute to offlineUI;
    exports execute.dto;
    exports logic.instructions to offlineUI;
}