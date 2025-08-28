package logic.instructions.api;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.labels.Label;

public abstract class AbstractInstruction implements Instruction {
    private final Label selfLabel;
    private final InstructionData data;

    public AbstractInstruction(InstructionData data) {
        this(data, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData data, Label selfLabel) {
        this.data = data;
        this.selfLabel = selfLabel;
    }

    @Override
    public Label getSelfLabel() { return selfLabel; }

    @Override
    public String getRepresentation(int num) {
        return String.format("#%d (%s) [ %-3s ] %s (%d)",
                num,
                data.getInstructionType(),
                selfLabel.getLabel(),
                this.print(),
                data.getCycles());
    }

    @Override
    public int getCycles() { return data.getCycles(); }

    @Override
    public int getDegree() { return data.getDegree(); }

    @Override
    public Label getTargetLabel() { return FixedLabel.EMPTY; }
}
