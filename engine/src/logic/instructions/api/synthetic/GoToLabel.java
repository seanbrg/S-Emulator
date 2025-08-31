package logic.instructions.api.synthetic;

import execute.dto.LabelDTO;
import execute.dto.VariableDTO;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.Label;

import java.util.List;


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
    public List<VariableDTO> getVarsDTO() { return List.of(); }

    @Override
    public LabelDTO getArgLabelDTO() { return new LabelDTO(target.getLabel()); }

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