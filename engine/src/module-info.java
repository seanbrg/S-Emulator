module engine {
    requires java.xml;

    exports execute to ui;
    exports execute.dto;
    exports logic.instructions to ui;
}