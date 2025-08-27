package logic.program;

import logic.variables.Variable;

import java.util.ArrayList;
import java.util.function.Supplier;

public class VariablesList extends ArrayList<Variable> {
    private final Supplier<? extends Variable> variableFactory;

    public VariablesList(Supplier<? extends Variable> variableFactory) {
        this.variableFactory = variableFactory;
    }

    @Override
    public Variable get(int index) {
        if (index < 0)
            return null;
        else if (index >= size()) {
            for (int i = index; i >= size(); i--) {
                this.add(variableFactory.get());
            }
            return this.get(index);
        }
        else {
            return super.get(index);
        }
    }
}
