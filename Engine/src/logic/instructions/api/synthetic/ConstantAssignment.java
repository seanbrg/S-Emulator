package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Var;
import logic.variables.Variable;


public class ConstantAssignment extends AbstractInstruction {

    private final Variable v;
    private final int k;

    public ConstantAssignment(Variable v, int k) {
        super(InstructionData.CONSTANT_ASSIGNMENT);
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