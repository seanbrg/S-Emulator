package logic.instructions;

import java.util.ArrayList;
import java.util.List;

public class ExpandedInstruction {
    private final Instruction instruction;
    private final List<Instruction> history;

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

    public String getRepresentation() {
        StringBuilder sb = new StringBuilder(instruction.getRepresentation());
        for (Instruction h : history) {
            sb.append("  <<<  ").append(h.getRepresentation());
        }
        return sb.toString();
    }

    public void setNum(int num) { this.instruction.setNum(num); }
}
