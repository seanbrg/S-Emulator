package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

public class JumpZero extends AbstractInstruction {

    private final Variable v;
    private final Label target;

    public JumpZero(Label selfLabel, Variable v, Label target, int num) {
        super(InstructionData.JUMP_ZERO,  selfLabel, num);
        this.v = v;
        this.target = target;
    }

    public JumpZero(Label selfLabel, Variable v, Label target) {
        this( selfLabel, v, target, 1);
    }

    @Override
    public Label execute() {
        if (v.getValue() == 0) return target;
        else return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return "IF " + v.getName() + " = 0 GOTO " + target.getLabel();
    }
    public Variable getVariable() { return v; }

    @Override
    public Label getTargetLabel() {
        return target;
    }
}
