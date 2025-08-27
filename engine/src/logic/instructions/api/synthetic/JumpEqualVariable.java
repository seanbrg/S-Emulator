package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;
import logic.variables.Variable;

public class JumpEqualVariable extends AbstractInstruction {

    private final Variable x;
    private final Label target;
    private final Variable y;

    public JumpEqualVariable(Label selfLabel, Variable x, Variable y, Label target) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, selfLabel);
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
