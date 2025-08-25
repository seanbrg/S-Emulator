package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;

public class JumpEqualConstant extends AbstractInstruction {

    private final Var v;
    private final Label target;
    private final int k;

    public JumpEqualConstant(Var v, int k, Label target, int num) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, num);
        this.v = v;
        this.target = target;
        this.k = k;
    }

    @Override
    public Label execute() {
        if (v.getValue() == k) return target;
        else return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return "IF " + v.getName() + " = " + k + " GOTO " + target.getLabel();
    }
}
