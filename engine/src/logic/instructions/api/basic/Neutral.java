package logic.instructions.api.basic;
import execute.dto.VariableDTO;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.List;


public class Neutral extends AbstractInstruction {

    private final Variable v;

    public Neutral(Label selfLabel, Variable v, int num) {
        super(InstructionData.NO_OP, selfLabel, num);
        this.v = v;
    }

    public Neutral(Label selfLabel, Variable v) {
        this(selfLabel, v, 1);
    }

    @Override
    public List<VariableDTO> getVarsDTO() { return List.of(new VariableDTO(v)); }

    @Override
    public Label execute() {
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        return v.getName() + " <- " + v.getName();
    }
}
