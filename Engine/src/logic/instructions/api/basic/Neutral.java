package logic.instructions.api.basic;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;


public class Neutral extends AbstractInstruction {

    private final Var v;

    public Neutral(Var v, int num) {
        super(InstructionData.NO_OP, num);
        this.v = v;
    }

    @Override
    public Label execute() {
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- " + v.getName();
    }
}
