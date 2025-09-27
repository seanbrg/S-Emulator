package execute;

import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import execute.dto.LabelDTO;
import execute.dto.VariableDTO;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram(int degree);

    long runProgram(String programName, int degree);

    HistoryDTO runProgramAndRecord(String programName, int degree, List<VariableDTO> inputs);

    void setPrintMode(boolean mode);

    boolean isLoaded();

    String getProgramName();

    void resetVars();

    boolean validateProgram(int degree);

    int maxDegree();

    List<List<VariableDTO>> getVarByType();

    List<VariableDTO> getOutputs();

    List<VariableDTO> getInputs();

    int getCycles(int degree);

    void fillOutVars(Map<String, Variable> vars);

    void loadInputs(List<VariableDTO> inputVars);

    List<InstructionDTO> getInstructionsList(String programName, int degree);

    List<InstructionDTO> getInstrParents(InstructionDTO selectedInstr);

    void debugStart(String programName, int degree, List<VariableDTO> inputs);
    
    boolean debugStep(String programName, int degree);

    int getDebugLine();

    List<LabelDTO> getLabels(String programName, int degree);
}
