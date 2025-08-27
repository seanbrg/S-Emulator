package logic.instructions.api.synthetic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;


public class ZeroVariable extends AbstractInstruction {

    private final Variable v;

    public ZeroVariable(Label selfLabel, Variable v) {
        super(InstructionData.ZERO_VARIABLE,  selfLabel);
        this.v = v;
    }

    @Override
    public Label execute() {
        v.setValue(0);
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- 0";
    }
}