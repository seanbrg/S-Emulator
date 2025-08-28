package logic.instructions.api.synthetic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;


public class ZeroVariable extends AbstractInstruction {

    private final Variable v;

    public ZeroVariable(Label selfLabel, Variable v, int num) {
        super(InstructionData.ZERO_VARIABLE,  selfLabel, num);
        this.v = v;
    }

    public ZeroVariable(Label selfLabel, Variable v) {
        this( selfLabel, v, 1);
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
    public Variable getVariable() { return v; }
}