package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;

public class JumpZero extends AbstractInstruction {

    private final Var v;
    private final Label target;

    public JumpZero(Var v, Label target, int num) {
        super(InstructionData.JUMP_ZERO, num);
        this.v = v;
        this.target = target;
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
}
