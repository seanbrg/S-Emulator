package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.Label;


public class GoToLabel extends AbstractInstruction {

    private final Label target;

    public GoToLabel(Label target, int num) {
        super(InstructionData.GOTO_LABEL, num);
        this.target = target;
    }

    @Override
    public Label execute() {
        return target;
    }

    @Override
    public String print() {
        return "GOTO " + target.getLabel();
    }
}