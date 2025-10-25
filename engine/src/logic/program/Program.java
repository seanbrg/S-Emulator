package logic.program;

import logic.instructions.Instruction;
import logic.labels.Label;
import logic.variables.Variable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program {
    public void run();
    void addInstruction(Instruction instruction);

    String getName();

    String getUserStr();

    List<Instruction> getInstructions();
    Map<Label, Instruction> getLabels();

    Set<Variable> getVariables();

    int maxDegree();
    int cycles();

    boolean checkLabels();

    void setInstrList(List<Instruction> newInstrList);

    void setLabelMap(Map<Label, Instruction> newLabelsMap);

    Map<Integer, Variable> getInputs();

    Variable getOutput();

    void setOutputVar(Variable newVar);

    void setInputs(Map<Integer, Variable> inputVars);

    void findVariables();

    int getArchVersion();
}
