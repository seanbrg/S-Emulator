package logic.instructions.api.synthetic;

import execute.dto.VariableDTO;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.ArrayList;
import java.util.List;


public class Assignment extends AbstractInstruction {

    private final Variable x, y;

    public Assignment(Label selfLabel, Variable x, Variable y, int num, Instruction parent) {
        super(InstructionData.ASSIGNMENT, selfLabel, num, parent);
        this.x = x;
        this.y = y;
    }

    public Assignment(Label selfLabel, Variable x, Variable y, int num) {
        this(selfLabel, x, y, num, null);
    }

    public Assignment(Label selfLabel, Variable x, Variable y) {
        this(selfLabel, x, y, 1);
    }

    @Override
    public List<VariableDTO> getVarsDTO() {
        List<VariableDTO> vars = new ArrayList<>(2);
        if (x != null) vars.add(new VariableDTO(x));
        if (y != null) vars.add(new VariableDTO(y));
        return vars;
    }

    @Override
    public List<Variable> getVars() {
        List<Variable> vars = new ArrayList<>(2);
        if (x != null) vars.add(x);
        if (y != null) vars.add(y);
        return vars;
    }

    @Override
    public Variable getPrimaryVar() { return x; }

    @Override
    public Variable getSecondaryVar() { return y; }


    @Override
    public Label execute() {
        x.setValue(y.getValue());
        return FixedLabel.EMPTY;
    }

    @Override
    public String print() {
        if (x == null || y == null) {
            return " :( ";
        }
        return x.getName() + " <- " + y.getName();
    }
    public Variable getX() { return x; }
    public Variable getY() { return y; }
}