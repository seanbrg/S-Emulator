package logic.instructions;
import logic.variables.Var;

public class Increase extends AbstractInstruction {

    private Var v;

    Increase(Var v, int num) {
        super(InstructionData.INCREASE, num);
        this.v = v;
    }

    @Override
    public void execute() {
        v.setValue(v.getValue() + 1);
    }
}
