package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;


public class Assignment extends AbstractInstruction {

    private final Variable x, y;

    public Assignment(Label selfLabel, Variable x, Variable y) {
        super(InstructionData.ASSIGNMENT,  selfLabel);
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
    public Variable getX() { return x; }
    public Variable getY() { return y; }
}