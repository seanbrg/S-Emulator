package logic.instructions;
import logic.labels.FixedLabel;
import logic.labels.Label;

public abstract class AbstractInstruction implements Instruction {
    private int num;
    private final Label label;
    private final InstructionData data;

    AbstractInstruction(InstructionData data, int num) {
        this(data, num, FixedLabel.EMPTY);
    }

    AbstractInstruction(InstructionData data, int num, Label label) {
        this.data = data;
        this.num = num;
        this.label = label;
    }

    @Override
    public Label getLabel() { return label; }

    @Override
    public String getRepresentation() {
        return "#" + num + " (" + data.getInstructionType().toString() + ") " +
                " [ " + label.getLabel() + " ] " + this.print() +
                " (" + data.getCycles() + ")";
    }

    @Override
    public int getCycles() { return data.getCycles(); }

    public String print() { return ""; }
}
