package execute;

import execute.dto.HistoryDTO;
import execute.dto.InstructionDTO;
import execute.dto.VariableDTO;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {
    boolean loadFromXML(String filePath);

    void printProgram(int degree);

    long runProgram(int degree);

    HistoryDTO runProgramAndRecord(int degree, List<VariableDTO> inputs);

    void setPrintMode(boolean mode);

    boolean isLoaded();

    String getProgramName();

    void resetVars();

    boolean validateProgram(int degree);

    int maxDegree();

    List<List<VariableDTO>> getVarByType();

    List<VariableDTO> getInputs();

    int getCycles(int degree);

    void fillOutVars(Map<String, Variable> vars);

    void loadInputs(List<VariableDTO> inputVars);

    List<InstructionDTO> getInstructionsList(String programName, int degree);

    List<InstructionDTO> getInstrParents(InstructionDTO selectedInstr);
}
