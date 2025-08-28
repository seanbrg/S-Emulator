package execute;

import logic.instructions.Instruction;

import java.util.ArrayList;
import java.util.List;

public class ExpandedInstruction {
    private final Instruction instruction;
    private final List<Instruction> history; // כל השרשרת עד כאן

    public ExpandedInstruction(Instruction instr) {
        this.instruction = instr;
        this.history = new ArrayList<>();
    }

    public ExpandedInstruction(Instruction instr, List<Instruction> parentHistory) {
        this.instruction = instr;
        this.history = new ArrayList<>(parentHistory);
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public List<Instruction> getHistory() {
        return history;
    }

    public String getRepresentation(int num) {
        StringBuilder sb = new StringBuilder(instruction.getRepresentation(num));
        for (Instruction h : history) {
            sb.append("  <<<  ").append(h.getRepresentation(-1));
        }
        return sb.toString();
    }
}
