package logic.instructions.api.synthetic;

import execute.components.ProgramManager;
import execute.dto.VariableDTO;
import logic.arguments.Argument;
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
    private final Program function;
    private final List<Argument> args;

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
        return String.format("%s <- (%s, %s)", v.getName(), function.getName(), argsStr);
    }

    @Override
    public Label execute() {
        List<Variable> argsVars = new ArrayList<>();

        for (Argument arg : args) {
            argsVars.add(arg.get());
        }
        System.out.println(argsVars.stream().map(Variable::getValue).toList());
        v.setValue(ProgramManager.runFunction(function, argsVars));
        return FixedLabel.EMPTY;
    }

    @Override
    public List<Variable> getVars() {
        return List.of(v);
    }

    @Override
    public List<VariableDTO> getVarsDTO() {
        return List.of(new VariableDTO(v));
    }
}
