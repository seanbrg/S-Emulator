package execute;

import execute.dto.*;
import logic.variables.Variable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram(String program, int degree);

    long runProgram(String programName, int degree);

    HistoryDTO runProgramAndRecord(String programName, int degree, List<VariableDTO> inputs);

    void setPrintMode(boolean mode);

    boolean isLoaded();

    String getFirstProgramName();

    void resetVars();

    boolean validateProgram(String program, int degree);

    int maxDegree(String func);

    List<List<VariableDTO>> getVarByType();

    List<VariableDTO> getOutputs(String func, int degree);

    List<VariableDTO> getInputs(String func, int degree);

    int getCycles(String program, int degree);

    boolean loadFromStream(InputStream inputStream);

    void fillOutVars(Map<String, Variable> vars);

    void loadInputs(List<VariableDTO> inputVars);

    List<InstructionDTO> getInstructionsList(String programName, int degree);

    void debugStart(String programName, int degree, List<VariableDTO> inputs);
    
    boolean debugStep(String programName, int degree);

    int getDebugLine();

    List<LabelDTO> getLabels(String programName, int degree);

    List<String> getFuncNamesList();

    void clear();

    HistoryDTO recordCurrentState(String programName, int degree, List<VariableDTO> inputs);

    ProgramDTO getProgramDTO(String programName, int degree);

    boolean isProgramExists(String programName, int degree);

    List<ProgramDTO> getAllProgramDTOs();

    List<String> getAllProgramNames();
}
