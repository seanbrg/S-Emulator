package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;


public class ConstantAssignment extends AbstractInstruction {

    private final Var v;
    private final int k;

    public ConstantAssignment(Var v, int k, int num) {
        super(InstructionData.CONSTANT_ASSIGNMENT, num);
        this.v = v;
        this.k = k;
    }

    @Override
    public Label execute() {
        v.setValue(k);
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- " + k;
    }
}