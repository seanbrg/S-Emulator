package logic.instructions.api.basic;
import execute.dto.VariableDTO;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.List;

public class Decrease extends AbstractInstruction {

    private final Variable v;

    public Decrease(Label selfLabel, Variable v, int num) {
        super(InstructionData.DECREASE, selfLabel, num);
        this.v = v;
    }

    public Decrease(Label selfLabel, Variable v) {
        this(selfLabel, v, 1);
    }

    @Override
    public List<VariableDTO> getVarsDTO() { return List.of(new VariableDTO(v)); }

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
