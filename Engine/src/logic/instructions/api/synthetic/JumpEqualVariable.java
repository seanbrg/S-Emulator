package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;

public class JumpEqualVariable extends AbstractInstruction {

    private final Var x;
    private final Label target;
    private final Var y;

    public JumpEqualVariable(Var x, Var y, Label target, int num) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, num);
        this.x = x;
        this.y = y;
        this.target = target;
    }

    @Override
    public Label execute() {
        if (x.getValue() == y.getValue()) return target;
        else return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return "IF " + x.getName() + " = " + y.getName() + " GOTO " + target.getLabel();
    }
}
