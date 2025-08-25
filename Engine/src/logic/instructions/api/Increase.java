package logic.instructions.api;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;

public class Increase extends AbstractInstruction {

    private Var v;

    Increase(Var v, int num) {
        super(InstructionData.INCREASE, num);
        this.v = v;
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
