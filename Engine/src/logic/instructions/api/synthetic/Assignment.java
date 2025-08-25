package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;


public class Assignment extends AbstractInstruction {

    private final Var x, y;

    public Assignment(Var x, Var y, int num) {
        super(InstructionData.ASSIGNMENT, num);
        this.x = x;
        this.y = y;
    }

    @Override
    public Label execute() {
        x.setValue(y.getValue());
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return x.getName() + " <- " + y.getName();
    }
}