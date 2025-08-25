package logic.instructions.api.synthetic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;


public class ZeroVariable extends AbstractInstruction {

    private final Var v;

    public ZeroVariable(Var v, int num) {
        super(InstructionData.ZERO_VARIABLE, num);
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