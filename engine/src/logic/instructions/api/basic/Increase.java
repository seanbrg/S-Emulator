package logic.instructions.api.basic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

public class Increase extends AbstractInstruction {

    private final Variable v;

    public Increase(Label selfLabel, Variable v, int num) {
        super(InstructionData.INCREASE, selfLabel, num);
        this.v = v;
    }

    public Increase(Label selfLabel, Variable v) {
        this(selfLabel, v, 1);
    }

    @Override
    public Label execute() {
        v.setValue(v.getValue() + 1);
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- "  + v.getName() + " + 1";
    }
}
