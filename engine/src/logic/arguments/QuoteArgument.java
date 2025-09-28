package logic.arguments;

import execute.components.ProgramManager;
import logic.instructions.api.synthetic.Quote;
import logic.variables.Variable;

public class QuoteArgument implements Argument {
    private final Quote quoteInstruction;

    public QuoteArgument(Quote quoteInstruction) {
        this.quoteInstruction = quoteInstruction;
    }

    @Override
    public String toString() {
        String printed = quoteInstruction.print();
        return printed.substring(printed.indexOf("-") + 2).trim();
    }

    @Override
    public Variable get() {
        quoteInstruction.execute();
        return quoteInstruction.getVars().getFirst();
    }
}
