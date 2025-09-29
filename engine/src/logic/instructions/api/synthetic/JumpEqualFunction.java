package logic.instructions.api.synthetic;

import execute.dto.VariableDTO;
import logic.arguments.QuoteArgument;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.variables.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualFunction extends AbstractInstruction {
    QuoteArgument quote;
    Variable v;
    Label target;

    public JumpEqualFunction(Label selfLabel, QuoteArgument quote, Variable v, Label target, int num, Instruction parent) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, selfLabel, num, parent);
        this.quote = quote;
        this.v = v;
        this.target = target;
    }

    public JumpEqualFunction(Label selfLabel, QuoteArgument quote, Variable v, Label target, int num) {
        this(selfLabel, quote, v, target, num, null);
    }

    public JumpEqualFunction(Label selfLabel, QuoteArgument quote, Variable v, Label target) {
        this(selfLabel, quote, v, target, 0, null);
    }


    @Override
    public String print() {
        return "IF " + v.getName() + " = " + quote + " GOTO " + target.getLabel();
    }

    @Override
    public Label execute() {
        if (v.getValue() == quote.get().getValue()) {
            return target;
        }
        else return FixedLabel.EMPTY;
    }

    @Override
    public List<Variable> getVars() {
        List<Variable> vars = new ArrayList<>();
        vars.add(v);
        vars.addAll(quote.getQuoteInstruction().getVars());
        return vars;
    }

    @Override
    public List<VariableDTO> getVarsDTO() {
        List<VariableDTO> vars = new ArrayList<>();
        vars.add(new VariableDTO(v));
        vars.addAll(quote.getQuoteInstruction().getVars().stream().map(VariableDTO::new).toList());
        return vars;
    }

    @Override
    public Variable getPrimaryVar() {
        return v;
    }

    @Override
    public Variable getSecondaryVar() {
        return v;
    }

    public QuoteArgument getQuoteArg() {
        return quote;
    }
}
