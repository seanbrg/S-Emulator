package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

public class JumpEqualConstant extends AbstractInstruction {

    private final Variable v;
    private final Label target;
    private final int k;

    public JumpEqualConstant(Label selfLabel, Variable v, int k, Label target, int num) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, selfLabel, num);
        this.v = v;
        this.target = target;
        this.k = k;
    }

    public JumpEqualConstant(Label selfLabel, Variable v, int k, Label target) {
        this( selfLabel, v, k, target, 1);
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
    public Variable getVariable() { return v; }
    public int getConstant() { return k; }

    @Override
    public Label getTargetLabel() {
        return target;
    }
}
