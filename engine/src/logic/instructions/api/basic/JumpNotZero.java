package logic.instructions.api.basic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

public class JumpNotZero extends AbstractInstruction {

    private final Variable v;
    private final Label target;

    public JumpNotZero(Label selfLabel, Variable v, Label target) {
        super(InstructionData.JUMP_NOT_ZERO, selfLabel);
        this.v = v;
        this.target = target;
    }

    @Override
    public Label execute() {
        if (v.getValue() != 0) return target;
        else return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return "IF " + v.getName() + " != 0 GOTO " + target.getLabel();
    }

    @Override
    public Label getTargetLabel() {
        return target;
    }
}
