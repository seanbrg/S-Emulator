package execute.sengine;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.labels.Label;

import java.util.List;
import java.util.Map;

public class SEngineAPI implements SEngine {
    List<Instruction> instructions;
    Map<Label, Instruction> labels;

    public SEngineAPI() {}


    @Override
    public void addInstruction(String instructionName) {

    }
}
