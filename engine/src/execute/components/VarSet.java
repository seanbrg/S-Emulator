package execute.components;

import logic.variables.Var;
import logic.variables.Variable;

import java.util.HashSet;
import java.util.function.Supplier;

// VariablesList( () -> new Var(VariableType.TEMP, tempVariables.size()) )
// a set of Vars that creates a new Var equal to 0 when it is first added

public class VarSet extends HashSet<Var> {
    private final Supplier<? extends Variable> variableFactory;

    public VarSet(Supplier<? extends Variable> variableFactory) {
        this.variableFactory = variableFactory;
    }
}


