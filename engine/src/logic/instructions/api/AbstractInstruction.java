package logic.instructions.api;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.labels.Label;

public abstract class AbstractInstruction implements Instruction {
    private final Label selfLabel;
    private final InstructionData data;
    private int num;

    public AbstractInstruction(InstructionData data) {
        this(data, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData data, Label selfLabel, int num) {
        this.data = data;
        this.selfLabel = selfLabel;
        this.num = num;
    }

    public AbstractInstruction(InstructionData data, Label selfLabel) {
        this(data, selfLabel, 0);
    }

    @Override
    public Label getSelfLabel() { return selfLabel; }

    @Override
    public String getRepresentation() {
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
    public void setNum(int num) { this.num = num; }

    @Override
    public Label getTargetLabel() { return FixedLabel.EMPTY; }

    @Override
    public int getNum() { return num; }
}
