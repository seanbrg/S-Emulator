package logic.instructions.api.synthetic;

import execute.components.ProgramManager;
import execute.dto.VariableDTO;
import logic.arguments.Argument;
import logic.arguments.QuoteArgument;
import logic.arguments.VarArgument;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.api.AbstractInstruction;
import logic.labels.FixedLabel;
import logic.labels.Label;
import logic.program.Program;
import logic.variables.Variable;

import java.util.ArrayList;
import java.util.List;

public class Quote extends AbstractInstruction {
    private final Variable v;
    private Program function;
    private List<Argument> args;

    public Quote(Label selfLabel, Variable v, Program function, List<Argument> args, int num, Instruction parent) {
        super(InstructionData.ZERO_VARIABLE,  selfLabel, num, parent);
        this.v = v;
        this.function = function;
        this.args = args;
    }

    public Quote(Label selfLabel, Variable v, Program function, List<Argument> args, int num) {
        this(selfLabel, v, function, args, num, null);
    }

    public Quote(Label selfLabel, Variable v, Program function, List<Argument> args) {
        this(selfLabel, v, function, args, 1);
    }

    @Override
    public String print() {
        String argsStr = String.join(",", args.stream().map(Argument::toString).toList());
        return String.format("%s <- (%s,%s)", v.getName(), function.getUserStr(), argsStr);
    }

    @Override
    public Label execute() {
        List<Variable> argsVars = new ArrayList<>();

        for (Argument arg : args) {
            argsVars.add(arg.get());
        }
        v.setValue(ProgramManager.runFunction(function, argsVars));
        return FixedLabel.EMPTY;
    }

    @Override
    public List<Variable> getVars() {
        List<Variable> result = new ArrayList<>();

        result.add(v);
        for (Argument arg : args) {
            if (arg instanceof VarArgument) {
                result.add(arg.get());
            }
            else if (arg instanceof QuoteArgument quo) {
                result.addAll(quo.getQuoteInstruction().getVars());
            }
        }
        return result;
    }

    @Override
    public List<VariableDTO> getVarsDTO() {
        List<VariableDTO> result = new ArrayList<>();

        result.add(new VariableDTO(v));
        for (Argument arg : args) {
            if (arg instanceof VarArgument) {
                result.add(new VariableDTO(arg.get()));
            }
            else if (arg instanceof QuoteArgument quo) {
                result.addAll(quo.getQuoteInstruction().getVarsDTO());
            }
        }
        return result;
    }

    @Override
    public Variable getPrimaryVar() {
        return v;
    }

    @Override
    public Variable getSecondaryVar() {
        return v;
    }

    @Override
    public int getCycles() {
        int cycles = 5;
        cycles += function.cycles();
        for (Argument arg : args) {
            if (arg instanceof QuoteArgument quo) {
                cycles += (quo.getQuoteInstruction().getCycles());
            }
        }
        return cycles;
    }

    @Override
    public int getDegree() {
        return 1 + function.maxDegree();
    }

    public Program getFunction() { return function; }

    public void setFunction(Program function) { this.function = function; }

    public List<Argument> getArgs() {
        return args;
    }

    public void setArgs(List<Argument> arguments) {
        this.args = arguments;
    }
}
