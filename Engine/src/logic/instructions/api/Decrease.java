package logic.instructions.api;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;

public class Decrease extends AbstractInstruction {

    private Var v;

    public Decrease(Var v, int num) {
        super(InstructionData.DECREASE, num);
        this.v = v;
    }

    @Override
    public Label execute() {
        v.setValue(v.getValue() - 1);
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- "  + v.getName() + " - 1";
    }
}
