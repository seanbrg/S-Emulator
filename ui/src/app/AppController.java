package app;

import app.header.HeaderController;
import execute.Engine;
import execute.EngineImpl;

import java.io.File;

public class AppController {
    private HeaderController headerController;
    private Engine engine;

    public void setHeaderController(HeaderController headerController) {
        this.headerController = headerController;
        this.headerController.setAppController(this);
    }

    public void setEngine(Engine engine) { this.engine = engine; }

    public void processFile(String selectedFilePath) {
        //System.out.println("Processing file: " + selectedFile.getAbsolutePath());
        boolean result = engine.loadFromXML(selectedFilePath);

        String programName = engine.getProgramName();
        int maxDegree = engine.maxDegree();
        headerController.fileLoadResult(result);

        if (result) headerController.setProgram(programName, maxDegree);
    }
}
