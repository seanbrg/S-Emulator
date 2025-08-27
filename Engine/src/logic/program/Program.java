package logic.program;

import logic.instructions.Instruction;
import logic.labels.Label;
import logic.variables.Variable;
import java.util.List;
import java.util.Map;

public interface Program {
    public Variable run(int degree, VariablesList inputVariables);
    void addInstruction(Instruction instruction);

    String getName();
    List<Instruction> getInstructions();
    VariablesList getVars();
    Map<Label, Instruction> getLabels();

    int maxDegree();
    int cycles();
}
