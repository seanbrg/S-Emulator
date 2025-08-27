package logic.program;

import logic.instructions.Instruction;
import logic.labels.Label;
import logic.variables.Variable;
import java.util.List;
import java.util.Map;

public interface Program {
    Variable run();
    void addInstruction(Instruction instruction);

    String getName();
    List<Instruction> getInstructions();
    Map<String, Variable> getVars();
    Map<Label, Instruction> getLabels();

    boolean validate();
    int maxDegree();
    int cycles();

}
