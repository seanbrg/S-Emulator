package logic.instructions.api;
import logic.instructions.InstructionData;
import logic.labels.FixedLabel;
import logic.variables.Var;
import logic.labels.Label;

public class JumpNotZero extends AbstractInstruction {

    private Var v;
    private Label target;

    JumpNotZero(Var v, Label target, int num) {
        super(InstructionData.JUMP_NOT_ZERO, num);
        this.v = v;
        this.target = target;
    }

    @Override
    public Label execute() {
        if (v.getValue() != 0) return target;
        else return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return "IF " + v.getValue() + " != 0 GOTO " + target.getLabel();
    }
}
