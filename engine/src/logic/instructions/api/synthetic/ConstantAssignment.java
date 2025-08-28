package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;


public class ConstantAssignment extends AbstractInstruction {

    private final Variable v;
    private final int k;

    public ConstantAssignment(Label selfLabel, Variable v, int k, int num) {
        super(InstructionData.CONSTANT_ASSIGNMENT, selfLabel, num);
        this.v = v;
        this.k = k;
    }

    public ConstantAssignment(Label selfLabel, Variable v, int k) {
        this(selfLabel, v, k, 1);
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
    public Variable getVariable() { return v; }
    public int getConstant() { return k; }

}