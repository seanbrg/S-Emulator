package logic.arguments;

import logic.variables.Variable;

public class VarArgument implements Argument {
    private final Variable v;

    public VarArgument(Variable v) {
        this.v = v;
    }

    @Override
    public String toString() { return v.getName(); }

    @Override
    public Variable get() { return v; }
}
