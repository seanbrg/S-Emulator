package logic.instructions.api.synthetic;

import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.Label;


public class GoToLabel extends AbstractInstruction {

    private final Label target;

    public GoToLabel(Label selfLabel, Label target, int num) {
        super(InstructionData.GOTO_LABEL,  selfLabel, num);
        this.target = target;
    }

    public GoToLabel(Label selfLabel, Label target) {
        this( selfLabel, target, 1);
    }

    @Override
    public Label execute() {
        return target;
    }

    @Override
    public String print() {
        return "GOTO " + target.getLabel();
    }

    @Override
    public Label getTargetLabel() {
        return target;
    }
}