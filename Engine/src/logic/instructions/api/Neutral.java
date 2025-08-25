package logic.instructions.api;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;


public class Neutral extends AbstractInstruction {

    private Var v;

    Neutral(Var v, int num) {
        super(InstructionData.NO_OP, num);
        this.v = v;
    }

    @Override
    public Label execute() {
        v.setValue(v.getValue());
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- " + v.getName();
    }
}
