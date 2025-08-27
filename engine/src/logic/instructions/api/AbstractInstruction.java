package logic.instructions.api;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.labels.Label;

public abstract class AbstractInstruction implements Instruction {
    private final Label label;
    private final InstructionData data;

    public AbstractInstruction(InstructionData data) {
        this(data, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData data, Label label) {
        this.data = data;
        this.label = label;
    }

    @Override
    public Label getLabel() { return label; }

    @Override
    public String getRepresentation(int num) {
        return "#" + num + " (" + data.getInstructionType().toString() + ") " +
                " [ " + label.getLabel() + " ] " + this.print() +
                " (" + data.getCycles() + ")";
    }

    @Override
    public int getCycles() { return data.getCycles(); }

    @Override
    public int getDegree() { return data.getDegree(); }
}
